package com.uruguaymnatural.backend.service;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Talks over HTTP to the simulated payment provider included in this project (/sandbox/payments).
 * Used when payments.provider=sandbox: it needs no account or keys, and its test cards can
 * also make the service fail or not answer, to try those cases.
 */
@Component
@ConditionalOnProperty(name = "payments.provider", havingValue = "sandbox")
public class SandboxPaymentGateway implements PaymentGateway {

    /** A charge is made either with the data of a new card or with the token of a saved one. */
    private record ChargeRequest(String cardNumber, String holder, String expiry, String cvc, String cardToken,
            long amount) {
    }

    private record ChargeResponse(String status, String transactionId, String cardToken, String reason) {
    }

    private record RefundRequest(long amount) {
    }

    private static final Logger log = LoggerFactory.getLogger(SandboxPaymentGateway.class);

    private final RestClient restClient;

    public SandboxPaymentGateway(@Value("${payments.sandbox.url}") String baseUrl,
            @Value("${payments.gateway.timeout-seconds}") long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));   // at most 1 minute (RNF-11)
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    @Override
    public String name() {
        return "sandbox";
    }

    @Override
    public Outcome charge(NewCard card, Order order) {
        return send(new ChargeRequest(card.number(), card.holder(), card.expiry(), card.cvc(), null,
                order.amount().longValue()));
    }

    @Override
    public Outcome chargeSavedCard(String cardToken, String cvc, Order order) {
        return send(new ChargeRequest(null, null, null, null, cardToken, order.amount().longValue()));
    }

    @Override
    public boolean refund(String transactionId, BigDecimal amount) {
        try {
            restClient.post()
                    .uri("/payments/{transactionId}/refund", transactionId)
                    .body(new RefundRequest(amount.longValue()))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.warn("El servicio de pagos no pudo hacer el reembolso de {}: {}", transactionId, e.toString());
            return false;
        }
    }

    private Outcome send(ChargeRequest request) {
        try {
            ChargeResponse response = restClient.post()
                    .uri("/payments")
                    .body(request)
                    .retrieve()
                    .body(ChargeResponse.class);
            if (response != null && "APPROVED".equals(response.status())) {
                return new Outcome(Result.APPROVED, response.transactionId(), response.cardToken(), null);
            }
            if (response != null && "REJECTED".equals(response.status())) {
                return new Outcome(Result.REJECTED, null, null, response.reason());
            }
            return new Outcome(Result.ERROR, null, null, "Respuesta desconocida del servicio de pagos");
        } catch (RestClientException e) {
            if (e instanceof ResourceAccessException || causedByTimeout(e)) {
                // Timeout or service unreachable.
                log.warn("El servicio de pagos no respondió: {}", e.toString());
                return new Outcome(Result.NO_RESPONSE, null, null, "El servicio de pagos no respondió a tiempo");
            }
            // The service answered with an error (for example HTTP 500).
            log.warn("El servicio de pagos devolvió un error: {}", e.toString());
            return new Outcome(Result.ERROR, null, null, "El servicio de pagos devolvió un error");
        }
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
