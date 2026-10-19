package com.uruguaymnatural.backend.service;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Talks over HTTP to the external payment service, Mercado Pago (RNF-10).
 *
 * With TEST credentials Mercado Pago works as a sandbox: no real money moves, and the result
 * depends on the test card holder's name (APRO = approved, FUND = insufficient funds, ...).
 *
 * It never lets an error of the external service escape: it always answers with one of four
 * outcomes, so the rest of the system can decide what to do (RNF-11).
 */
@Component
public class PaymentGatewayClient {

    public enum Result { APPROVED, REJECTED, PENDING, ERROR, NO_RESPONSE }

    /** What Mercado Pago answered. */
    public record Outcome(Result result, String paymentId, String cardLast4, String message) {
    }

    /** Data to charge: the card arrives as a token created by Mercado Pago in the browser. */
    public record Charge(BigDecimal amount, String cardToken, String paymentMethodId, String issuerId,
            String description, String payerEmail, String identificationType, String identificationNumber) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MpCard(@JsonProperty("last_four_digits") String lastFourDigits) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record MpPayment(Long id, String status, @JsonProperty("status_detail") String statusDetail, MpCard card) {
    }

    /** Mercado Pago's status_detail codes, in words for the user. */
    private static final Map<String, String> REJECTION_REASONS = Map.of(
            "cc_rejected_insufficient_amount", "Fondos insuficientes",
            "cc_rejected_bad_filled_security_code", "El código de seguridad es incorrecto",
            "cc_rejected_bad_filled_date", "La fecha de vencimiento es incorrecta",
            "cc_rejected_bad_filled_other", "Revisá los datos de la tarjeta",
            "cc_rejected_call_for_authorize", "Hay que autorizar el pago con el banco",
            "cc_rejected_card_disabled", "La tarjeta está deshabilitada",
            "cc_rejected_duplicated_payment", "Ya hiciste un pago igual",
            "cc_rejected_high_risk", "El pago fue rechazado por seguridad",
            "cc_rejected_blacklist", "El pago fue rechazado por seguridad");

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayClient.class);

    private final RestClient restClient;
    private final String accessToken;

    public PaymentGatewayClient(@Value("${payments.gateway.url}") String baseUrl,
            @Value("${payments.gateway.access-token}") String accessToken,
            @Value("${payments.gateway.timeout-seconds}") long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));   // at most 1 minute (RNF-11)
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.accessToken = accessToken;
    }

    public Outcome charge(Charge charge) {
        if (accessToken == null || accessToken.isBlank()) {
            return new Outcome(Result.ERROR, null, null, "Falta configurar el Access Token de Mercado Pago (MP_ACCESS_TOKEN)");
        }
        Map<String, Object> body = Map.of(
                "transaction_amount", charge.amount(),
                "token", charge.cardToken(),
                "description", charge.description(),
                "installments", 1,
                "payment_method_id", charge.paymentMethodId(),
                "payer", Map.of(
                        "email", charge.payerEmail(),
                        "identification", Map.of(
                                "type", charge.identificationType(),
                                "number", charge.identificationNumber())));
        Map<String, Object> request = charge.issuerId() == null || charge.issuerId().isBlank()
                ? body : withIssuer(body, charge.issuerId());
        try {
            MpPayment payment = restClient.post()
                    .uri("/v1/payments")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("X-Idempotency-Key", UUID.randomUUID().toString())   // never charge twice by accident
                    .body(request)
                    .retrieve()
                    .body(MpPayment.class);
            return toOutcome(payment);
        } catch (HttpClientErrorException e) {
            // Mercado Pago refused the request itself (bad token, wrong data, bad credentials).
            log.warn("Mercado Pago rechazó la solicitud: {} {}", e.getStatusCode(), e.getResponseBodyAsString());
            return new Outcome(Result.ERROR, null, null, "Mercado Pago no aceptó los datos del pago");
        } catch (RestClientException e) {
            if (e instanceof ResourceAccessException || causedByTimeout(e)) {
                log.warn("Mercado Pago no respondió: {}", e.toString());
                return new Outcome(Result.NO_RESPONSE, null, null, "El servicio de pagos no respondió a tiempo");
            }
            log.warn("Mercado Pago devolvió un error: {}", e.toString());
            return new Outcome(Result.ERROR, null, null, "El servicio de pagos devolvió un error");
        }
    }

    private static Outcome toOutcome(MpPayment payment) {
        if (payment == null || payment.status() == null) {
            return new Outcome(Result.ERROR, null, null, "Respuesta desconocida del servicio de pagos");
        }
        String id = payment.id() == null ? null : String.valueOf(payment.id());
        String last4 = payment.card() == null ? null : payment.card().lastFourDigits();
        return switch (payment.status()) {
            case "approved" -> new Outcome(Result.APPROVED, id, last4, null);
            case "rejected" -> new Outcome(Result.REJECTED, id, last4,
                    REJECTION_REASONS.getOrDefault(payment.statusDetail(), "El pago fue rechazado"));
            case "in_process", "pending", "authorized" -> new Outcome(Result.PENDING, id, last4,
                    "El pago quedó en revisión en Mercado Pago");
            default -> new Outcome(Result.ERROR, id, last4, "Estado desconocido: " + payment.status());
        };
    }

    private static Map<String, Object> withIssuer(Map<String, Object> body, String issuerId) {
        java.util.HashMap<String, Object> copy = new java.util.HashMap<>(body);
        copy.put("issuer_id", issuerId);
        return copy;
    }

    private static boolean causedByTimeout(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SocketTimeoutException || t instanceof ConnectException
                    || t instanceof HttpTimeoutException) {
                return true;
            }
        }
        return false;
    }
}
