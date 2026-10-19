package com.uruguaymnatural.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.PaymentDto;
import com.uruguaymnatural.backend.dto.PaymentRequestDto;
import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;
import com.uruguaymnatural.backend.repository.PaymentRepository;

/**
 * Payments through the external payment service, Mercado Pago (CU-16).
 *
 * Uruguay Mnatural does not process payments (RNF-10): it asks Mercado Pago and records what it
 * answered. Only an approved answer counts as paid (RN-19); if the card is rejected, the payment
 * stays under review, or the service fails or does not answer in time, nothing is confirmed (RNF-11).
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
        PaymentGatewayClient.Outcome outcome = gateway.charge(new PaymentGatewayClient.Charge(
                data.amount(), data.cardToken(), data.paymentMethodId(), data.issuerId(),
                "Uruguay Mnatural - pago de prueba", data.payerEmail().trim(),
                data.identificationType(), data.identificationNumber().trim()));

        PaymentStatus status = switch (outcome.result()) {
            case APPROVED -> PaymentStatus.APPROVED;
            case REJECTED -> PaymentStatus.REJECTED;
            case PENDING, ERROR, NO_RESPONSE -> PaymentStatus.PENDING;   // not confirmed
        };
        Payment payment = new Payment(data.amount(), status, outcome.cardLast4(), outcome.paymentId(),
                outcome.message());
        paymentRepository.save(payment);
        return PaymentDto.from(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> latest() {
        return paymentRepository.findTop20ByOrderByCreatedAtDesc().stream().map(PaymentDto::from).toList();
    }
}
