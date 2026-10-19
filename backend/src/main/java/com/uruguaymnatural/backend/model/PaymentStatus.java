package com.uruguaymnatural.backend.model;

/**
 * State of a payment attempt (PAGO.estado in the MER).
 *
 * PENDING  = the payment service did not answer or failed: nothing was charged and nothing is confirmed.
 * APPROVED = the payment service charged the card.
 * REJECTED = the payment service refused the card.
 * REFUNDED = the money was given back (used once bookings exist).
 */
public enum PaymentStatus {
    PENDING,
    APPROVED,
    REJECTED,
    REFUNDED
}
