package com.uruguaymnatural.backend.service;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Talks over HTTP to the Mercado Pago API (RNF-10). Used when payments.provider=mercadopago.
 *
 * Only its REST API is used, called from this backend: not its checkout nor its components
 * for the page. The card form is ours; the backend sends the card to Mercado Pago, which
 * answers with a one-use token, and then asks it to charge that token.
 *
 * With TEST credentials Mercado Pago works as a sandbox: no real money moves, and with its
 * test cards the result depends on the holder's name (APRO = approved, OTHE = rejected,
 * FUND = insufficient funds, CONT = left pending).
 */
@Component
@ConditionalOnProperty(name = "payments.provider", havingValue = "mercadopago", matchIfMissing = true)
public class MercadoPagoGateway implements PaymentGateway {

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
            new ParameterizedTypeReference<>() { };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> JSON_LIST =
            new ParameterizedTypeReference<>() { };

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
            "cc_rejected_blacklist", "El pago fue rechazado por seguridad",
            "cc_rejected_other_reason", "La tarjeta rechazó el pago");

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoGateway.class);

    private final RestClient restClient;
    private final String accessToken;
    private final String testPayerEmail;

    public MercadoPagoGateway(@Value("${payments.mercadopago.url}") String baseUrl,
            @Value("${payments.mercadopago.access-token}") String accessToken,
            @Value("${payments.mercadopago.test-payer-email}") String testPayerEmail,
            @Value("${payments.gateway.timeout-seconds}") long timeoutSeconds) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));   // at most 1 minute (RNF-11)
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.accessToken = accessToken;
        this.testPayerEmail = testPayerEmail;
    }

    @Override
    public String name() {
        return "mercadopago";
    }

    // ---- Charging ----

    @Override
    public Outcome charge(NewCard card, Order order) {
        return guarded(() -> {
            Map<String, Object> token = createCardToken(newCardBody(card, order));
            String firstSix = String.valueOf(token.get("first_six_digits"));
            Map<String, Object> payer = new HashMap<>();
            payer.put("email", payerEmail(order));
            identification(order).ifPresent(id -> payer.put("identification", id));

            Outcome outcome = pay(String.valueOf(token.get("id")), paymentMethodOf(firstSix, order), payer, order);
            if (outcome.result() != Result.APPROVED) {
                return outcome;
            }
            // RF-21: keep the card for the next time. If this fails the payment is still good:
            // the card is simply not saved.
            return new Outcome(Result.APPROVED, outcome.transactionId(), saveCard(card, order), null);
        });
    }

    @Override
    public Outcome chargeSavedCard(String cardToken, String cvc, Order order) {
        // The token we keep of a saved card is "customer:card:paymentMethod" (see saveCard).
        String[] parts = cardToken.split(":");
        if (parts.length != 3) {
            return new Outcome(Result.ERROR, null, null, "Esta tarjeta guardada no sirve con Mercado Pago");
        }
        return guarded(() -> {
            // Mercado Pago asks for the security code again every time a saved card is used.
            Map<String, Object> token = createCardToken(Map.of(
                    "customer_id", parts[0], "card_id", parts[1], "security_code", cvc == null ? "" : cvc));
            Map<String, Object> payer = Map.of("type", "customer", "id", parts[0]);
            Outcome outcome = pay(String.valueOf(token.get("id")), parts[2], payer, order);
            return new Outcome(outcome.result(), outcome.transactionId(),
                    outcome.result() == Result.APPROVED ? cardToken : null, outcome.reason());
        });
    }

    /** POST /v1/payments — asks Mercado Pago to charge a card token. */
    private Outcome pay(String token, String paymentMethodId, Map<String, Object> payer, Order order) {
        Map<String, Object> body = new HashMap<>();
        body.put("transaction_amount", order.amount());
        body.put("token", token);
        body.put("description", order.description());
        body.put("installments", 1);
        body.put("payment_method_id", paymentMethodId);
        body.put("payer", payer);

        Map<String, Object> payment = restClient.post()
                .uri("/v1/payments")
                .header("Authorization", "Bearer " + accessToken)
                .header("X-Idempotency-Key", UUID.randomUUID().toString())   // never charge twice by accident
                .body(body)
                .retrieve()
                .body(JSON_OBJECT);
        return toOutcome(payment);
    }

    private Outcome toOutcome(Map<String, Object> payment) {
        if (payment == null || payment.get("status") == null) {
            return new Outcome(Result.ERROR, null, null, "Respuesta desconocida del servicio de pagos");
        }
        String id = String.valueOf(payment.get("id"));
        String status = String.valueOf(payment.get("status"));
        return switch (status) {
            case "approved" -> new Outcome(Result.APPROVED, id, null, null);
            case "rejected" -> new Outcome(Result.REJECTED, null, null,
                    REJECTION_REASONS.getOrDefault(String.valueOf(payment.get("status_detail")),
                            "El pago fue rechazado"));
            case "in_process", "pending", "authorized" -> {
                // Mercado Pago left it "in review". We do not wait for it: it is cancelled so
                // nothing gets charged later for a booking that was not confirmed.
                cancel(id);
                yield new Outcome(Result.ERROR, null, null, "Mercado Pago dejó el pago en revisión y se anuló");
            }
            default -> new Outcome(Result.ERROR, null, null, "Estado desconocido del pago: " + status);
        };
    }

    /** PUT /v1/payments/{id} — cancels a payment that was not approved yet. */
    private void cancel(String paymentId) {
        try {
            restClient.put()
                    .uri("/v1/payments/{id}", paymentId)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(Map.of("status", "cancelled"))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.warn("No se pudo anular el pago {} en Mercado Pago: {}", paymentId, e.toString());
        }
    }

    // ---- Refunds ----

    /** POST /v1/payments/{id}/refunds — gives back part or all of an approved payment. */
    @Override
    public boolean refund(String transactionId, BigDecimal amount) {
        try {
            restClient.post()
                    .uri("/v1/payments/{id}/refunds", transactionId)
                    .header("Authorization", "Bearer " + accessToken)
                    .header("X-Idempotency-Key", UUID.randomUUID().toString())
                    .body(Map.of("amount", amount))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.warn("Mercado Pago no pudo hacer el reembolso del pago {}: {}", transactionId, describe(e));
            return false;
        }
    }

    // ---- Cards ----

    /** POST /v1/card_tokens — Mercado Pago turns card data into a token that can be used once. */
    private Map<String, Object> createCardToken(Map<String, Object> body) {
        Map<String, Object> token = restClient.post()
                .uri("/v1/card_tokens")
                .header("Authorization", "Bearer " + accessToken)
                .body(body)
                .retrieve()
                .body(JSON_OBJECT);
        if (token == null || token.get("id") == null) {
            throw new IllegalStateException("Mercado Pago no devolvió el token de la tarjeta");
        }
        return token;
    }

    private Map<String, Object> newCardBody(NewCard card, Order order) {
        Map<String, Object> holder = new HashMap<>();
        holder.put("name", card.holder());
        identification(order).ifPresent(id -> holder.put("identification", id));

        Map<String, Object> body = new HashMap<>();
        body.put("card_number", card.number());
        body.put("expiration_month", Integer.parseInt(card.expiry().substring(0, 2)));   // "12/29" -> 12
        body.put("expiration_year", 2000 + Integer.parseInt(card.expiry().substring(3, 5)));   // -> 2029
        body.put("security_code", card.cvc());
        body.put("cardholder", holder);
        return body;
    }

    /**
     * The kind of card (visa, master...) that Mercado Pago needs to charge. It is asked to
     * Mercado Pago with the first 6 digits; if that fails, it is guessed from the first digit.
     */
    private String paymentMethodOf(String firstSix, Order order) {
        try {
            List<Map<String, Object>> options = restClient.get()
                    .uri(uri -> uri.path("/v1/payment_methods/installments")
                            .queryParam("bin", firstSix)
                            .queryParam("amount", order.amount())
                            .build())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(JSON_LIST);
            if (options != null && !options.isEmpty() && options.get(0).get("payment_method_id") != null) {
                return String.valueOf(options.get(0).get("payment_method_id"));
            }
        } catch (RestClientException e) {
            log.warn("Mercado Pago no informó el tipo de tarjeta: {}", describe(e));
        }
        return switch (firstSix.charAt(0)) {
            case '4' -> "visa";
            case '3' -> "amex";
            default -> "master";
        };
    }

    /**
     * Saves the card in Mercado Pago under a "customer" with the tourist's email, and returns
     * our token for it: "customer:card:paymentMethod". null if it could not be saved.
     */
    private String saveCard(NewCard card, Order order) {
        try {
            String customerId = findOrCreateCustomer(payerEmail(order));
            // A card token works only once and the first one was spent on the payment.
            String token = String.valueOf(createCardToken(newCardBody(card, order)).get("id"));
            Map<String, Object> saved = restClient.post()
                    .uri("/v1/customers/{id}/cards", customerId)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(Map.of("token", token))
                    .retrieve()
                    .body(JSON_OBJECT);
            Object paymentMethod = saved == null ? null : saved.get("payment_method");
            if (saved == null || saved.get("id") == null || !(paymentMethod instanceof Map<?, ?> method)) {
                return null;
            }
            return customerId + ":" + saved.get("id") + ":" + method.get("id");
        } catch (RuntimeException e) {
            log.warn("El pago se aprobó pero la tarjeta no se pudo guardar en Mercado Pago: {}", describe(e));
            return null;
        }
    }

    private String findOrCreateCustomer(String email) {
        Map<String, Object> search = restClient.get()
                .uri(uri -> uri.path("/v1/customers/search").queryParam("email", email).build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(JSON_OBJECT);
        if (search != null && search.get("results") instanceof List<?> results && !results.isEmpty()
                && results.get(0) instanceof Map<?, ?> customer) {
            return String.valueOf(customer.get("id"));
        }
        Map<String, Object> created = restClient.post()
                .uri("/v1/customers")
                .header("Authorization", "Bearer " + accessToken)
                .body(Map.of("email", email))
                .retrieve()
                .body(JSON_OBJECT);
        if (created == null || created.get("id") == null) {
            throw new IllegalStateException("Mercado Pago no creó el cliente");
        }
        return String.valueOf(created.get("id"));
    }

    // ---- Helpers ----

    /** With test credentials Mercado Pago can ask that the payer be one of its test users. */
    private String payerEmail(Order order) {
        return testPayerEmail == null || testPayerEmail.isBlank() ? order.payerEmail() : testPayerEmail;
    }

    /** A Uruguayan ID (7 or 8 digits) is sent as "CI"; other documents are not sent. */
    private static java.util.Optional<Map<String, Object>> identification(Order order) {
        String digits = order.payerDocument() == null ? "" : order.payerDocument().replaceAll("\\D", "");
        if (digits.length() < 7 || digits.length() > 8) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(Map.of("type", "CI", "number", digits));
    }

    /** Runs the calls of one charge and turns any failure into an outcome (RNF-11). */
    private Outcome guarded(java.util.function.Supplier<Outcome> calls) {
        if (accessToken == null || accessToken.isBlank()) {
            return new Outcome(Result.ERROR, null, null,
                    "Falta configurar el Access Token de prueba de Mercado Pago");
        }
        try {
            return calls.get();
        } catch (RestClientResponseException e) {
            // Mercado Pago answered with an error: wrong card data (4xx) or a failure of theirs (5xx).
            log.warn("Mercado Pago devolvió un error: {}", describe(e));
            int code = e.getStatusCode().value();
            if (code == 401 || code == 403) {
                return new Outcome(Result.ERROR, null, null, "Mercado Pago no aceptó las credenciales de prueba");
            }
            return e.getStatusCode().is4xxClientError()
                    ? new Outcome(Result.REJECTED, null, null, "Mercado Pago no aceptó los datos de la tarjeta")
                    : new Outcome(Result.ERROR, null, null, "El servicio de pagos devolvió un error");
        } catch (RestClientException e) {
            if (e instanceof ResourceAccessException || causedByTimeout(e)) {
                log.warn("Mercado Pago no respondió: {}", e.toString());
                return new Outcome(Result.NO_RESPONSE, null, null, "El servicio de pagos no respondió a tiempo");
            }
            log.warn("Mercado Pago devolvió un error: {}", e.toString());
            return new Outcome(Result.ERROR, null, null, "El servicio de pagos devolvió un error");
        } catch (RuntimeException e) {
            log.warn("Respuesta inesperada de Mercado Pago: {}", e.toString());
            return new Outcome(Result.ERROR, null, null, "Respuesta desconocida del servicio de pagos");
        }
    }

    /** Status and body of Mercado Pago's error answer, for the log. */
    private static String describe(RuntimeException e) {
        return e instanceof RestClientResponseException response
                ? response.getStatusCode() + " " + response.getResponseBodyAsString()
                : e.toString();
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
