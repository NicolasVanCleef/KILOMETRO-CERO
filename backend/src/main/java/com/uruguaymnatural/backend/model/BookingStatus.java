package com.uruguaymnatural.backend.model;

/**
 * States of a booking (see "Estados de una reserva" in the business rules).
 *
 * PENDING_PAYMENT = the places are held for the tourist but it is not paid yet: first for
 *                   10 minutes while they finish the booking, then for 24 hours if the payment failed.
 * CONFIRMED       = paid, or the activity is free.
 * EXPIRED         = the time to pay ran out; the places were released automatically.
 * CANCELLED       = cancelled by the tourist, or because the provider or the administrator
 *                   cancelled the date or the activity.
 */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    EXPIRED,
    CANCELLED
}
