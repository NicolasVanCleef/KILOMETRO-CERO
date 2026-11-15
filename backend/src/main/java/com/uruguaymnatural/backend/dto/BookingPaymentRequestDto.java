package com.uruguaymnatural.backend.dto;

/**
 * Data to pay a booking (CU-16, RF-21): either a saved card (cardId) or a new card
 * (number, holder, expiry and security code). The amount is not sent: it is the booking's total.
 */
public record BookingPaymentRequestDto(
        Long cardId,
        String cardNumber,
        String holder,
        String expiry,
        String cvc) {
}
