package com.uruguaymnatural.backend.service;

import java.math.BigDecimal;

/**
 * The external payment service, seen from Uruguay Mnatural (RNF-10).
 *
 * There are two implementations and application.properties chooses one with payments.provider:
 * {@link MercadoPagoGateway} calls the Mercado Pago API with test credentials, and
 * {@link SandboxPaymentGateway} calls the simulated provider included in this project.
 * The rest of the system only knows this interface, so changing provider touches nothing else.
 *
 * An implementation never lets an error of the external service escape: it always answers
 * with one of four outcomes, so the caller can decide what to do (RNF-11).
 */
public interface PaymentGateway {

    enum Result { APPROVED, REJECTED, ERROR, NO_RESPONSE }

    /**
     * What the external service answered.
     *
     * @param transactionId its id of the charge; only when it was approved
     * @param cardToken     its reference to the card, to charge it again later; null if the
     *                      card could not be saved
     * @param reason        why it was not approved, in words for the user
     */
    record Outcome(Result result, String transactionId, String cardToken, String reason) {
    }

    /** The data of a card typed by the tourist. It is sent to the service and never stored. */
    record NewCard(String number, String holder, String expiry, String cvc) {
    }

    /** What is being paid and by whom. */
    record Order(BigDecimal amount, String description, String payerEmail, String payerDocument) {
    }

    /** Short name of the provider ("mercadopago" or "sandbox"), so the page shows the right test cards. */
    String name();

    /** Charges a new card. */
    Outcome charge(NewCard card, Order order);

    /** Charges a card saved before, identified by the token this service gave for it. */
    Outcome chargeSavedCard(String cardToken, String cvc, Order order);

    /** Asks the service to give money back from an approved charge. true if it did. */
    boolean refund(String transactionId, BigDecimal amount);
}
