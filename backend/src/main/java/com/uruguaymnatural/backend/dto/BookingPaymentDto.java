package com.uruguaymnatural.backend.dto;

/**
 * Answer of a payment: how the attempt went and how the booking is after it
 * (confirmed if it was approved, still pending if not).
 */
public record BookingPaymentDto(PaymentDto payment, BookingDto booking) {
}
