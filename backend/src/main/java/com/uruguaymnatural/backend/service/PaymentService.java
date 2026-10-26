package com.uruguaymnatural.backend.service;

import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.PaymentDto;
import com.uruguaymnatural.backend.dto.PaymentRequestDto;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;
import com.uruguaymnatural.backend.repository.PaymentRepository;

/**
 * Payments through the external payment service (CU-16).
 *
 * Uruguay Mnatural does not process payments (RNF-10): it asks the external service and records
 * what it answered. Only an approved answer counts as paid (RN-19); if the service rejects the card,
 * fails or does not answer in time, nothing is confirmed (RNF-11).
 */
@Service
public class PaymentService {

    private final PaymentGatewayClient gateway;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentGatewayClient gateway, PaymentRepository paymentRepository) {
        this.gateway = gateway;
        this.paymentRepository = paymentRepository;
    }

    // No @Transactional here: the call to the payment service can take up to a minute and must not
    // hold a database connection meanwhile. save() opens its own short transaction.
    public PaymentDto pay(PaymentRequestDto data) {
        String cardNumber = data.cardNumber().replaceAll("\\D", "");
        if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            throw new InvalidDataException("cardNumber", "El número de tarjeta tiene que tener entre 13 y 19 dígitos.");
        }
        if (isExpired(data.expiry())) {
            throw new InvalidDataException("expiry", "La tarjeta está vencida.");
        }

        PaymentGatewayClient.Outcome outcome = gateway.charge(cardNumber, data.holder().trim(), data.expiry(),
                data.cvc(), data.amount().longValue());

        PaymentStatus status = switch (outcome.result()) {
            case APPROVED -> PaymentStatus.APPROVED;
            case REJECTED -> PaymentStatus.REJECTED;
            case ERROR, NO_RESPONSE -> PaymentStatus.PENDING;   // not charged, not confirmed
        };
        String last4 = cardNumber.substring(cardNumber.length() - 4);   // RN-24: never the full number
        Payment payment = new Payment(data.amount(), status, last4, outcome.cardToken(), outcome.transactionId(),
                outcome.reason());
        paymentRepository.save(payment);
        return PaymentDto.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> latest() {
        return paymentRepository.findTop20ByOrderByCreatedAtDesc().stream().map(PaymentDto::from).toList();
    }

    /** A card is valid until the last day of its expiry month (MM/AA). */
    private static boolean isExpired(String expiry) {
        int month = Integer.parseInt(expiry.substring(0, 2));
        int year = 2000 + Integer.parseInt(expiry.substring(3, 5));
        return YearMonth.of(year, month).isBefore(YearMonth.now());
    }
}
