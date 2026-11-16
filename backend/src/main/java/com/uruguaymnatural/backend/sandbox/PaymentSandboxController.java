package com.uruguaymnatural.backend.sandbox;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulated EXTERNAL payment provider (sandbox), like the ones payment companies offer for testing.
 *
 * It is not part of Uruguay Mnatural: it lives in this project only so the course can run without a
 * real provider. With payments.provider=sandbox, Uruguay Mnatural talks to it over HTTP
 * (see SandboxPaymentGateway) instead of talking to Mercado Pago.
 *
 * The result depends on the card number, as in real sandboxes:
 *   4111 1111 1111 1111 → approved (any other valid number is approved too)
 *   4000 0000 0000 0002 → rejected
 *   4000 0000 0000 0500 → the service fails (HTTP 500)
 *   4000 0000 0000 0408 → the service does not answer in time
 * A charge with the token of a saved card is always approved.
 */
@RestController
@RequestMapping("/sandbox/payments")
public class PaymentSandboxController {

    public static final String CARD_REJECTED = "4000000000000002";
    public static final String CARD_ERROR = "4000000000000500";
    public static final String CARD_NO_RESPONSE = "4000000000000408";

    /** It waits a bit longer than Uruguay Mnatural waits, so the call times out. */
    private final long noResponseDelayMillis;

    public PaymentSandboxController(@Value("${payments.gateway.timeout-seconds}") long timeoutSeconds) {
        this.noResponseDelayMillis = (timeoutSeconds + 5) * 1000;
    }

    public record ChargeRequest(String cardNumber, String holder, String expiry, String cvc, String cardToken,
            long amount) {
    }

    public record ChargeResponse(String status, String transactionId, String cardToken, String reason) {
    }

    public record RefundRequest(long amount) {
    }

    public record RefundResponse(String status, String refundId) {
    }

    /** POST /sandbox/payments — charges a card. */
    @PostMapping
    public ResponseEntity<ChargeResponse> charge(@RequestBody ChargeRequest request) throws InterruptedException {
        if (request.cardToken() != null) {
            return ResponseEntity.ok(new ChargeResponse("APPROVED", "sbx_" + shortId(), request.cardToken(), null));
        }
        String card = request.cardNumber() == null ? "" : request.cardNumber().replaceAll("\\D", "");
        switch (card) {
            case CARD_NO_RESPONSE -> {
                Thread.sleep(noResponseDelayMillis);
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
            }
            case CARD_ERROR -> {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            case CARD_REJECTED -> {
                return ResponseEntity.ok(new ChargeResponse("REJECTED", null, null, "Fondos insuficientes"));
            }
            default -> {
                return ResponseEntity.ok(new ChargeResponse("APPROVED", "sbx_" + shortId(), "tok_" + shortId(), null));
            }
        }
    }

    /** POST /sandbox/payments/{transactionId}/refund — gives money back from an approved charge. */
    @PostMapping("/{transactionId}/refund")
    public RefundResponse refund(@PathVariable String transactionId, @RequestBody RefundRequest request) {
        return new RefundResponse("REFUNDED", "rfd_" + shortId());
    }

    private static String shortId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
