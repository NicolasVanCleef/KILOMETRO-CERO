package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;

/**
 * Result of a payment attempt. Never includes the card number nor its token.
 */
public record PaymentDto(
        Long id,
        BigDecimal amount,
        PaymentStatus status,
        String cardLast4,
        String externalReference,
        String message,
        LocalDateTime createdAt) {

    public static PaymentDto from(Payment payment) {
        return new PaymentDto(payment.getId(), payment.getAmount(), payment.getStatus(), payment.getCardLast4(),
                payment.getExternalReference(), payment.getMessage(), payment.getCreatedAt());
    }
}
