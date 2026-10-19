package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.uruguaymnatural.backend.dto.PaymentDto;
import com.uruguaymnatural.backend.dto.PaymentRequestDto;
import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;
import com.uruguaymnatural.backend.repository.PaymentRepository;
import com.uruguaymnatural.backend.service.PaymentGatewayClient.Charge;
import com.uruguaymnatural.backend.service.PaymentGatewayClient.Outcome;
import com.uruguaymnatural.backend.service.PaymentGatewayClient.Result;

class PaymentServiceTest {

    private PaymentGatewayClient gateway;
    private PaymentRepository paymentRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        gateway = mock(PaymentGatewayClient.class);
        paymentRepository = mock(PaymentRepository.class);
        paymentService = new PaymentService(gateway, paymentRepository);
    }

    @Test
    void approvedPaymentIsSavedWithMercadoPagoIdAndLast4() {
        answer(new Outcome(Result.APPROVED, "123456789", "3704", null));

        PaymentDto result = paymentService.pay(request());

        assertEquals(PaymentStatus.APPROVED, result.status());
        assertEquals("3704", result.cardLast4());
        assertEquals("123456789", result.externalReference());
        ArgumentCaptor<Payment> saved = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(saved.capture());
        assertEquals(PaymentStatus.APPROVED, saved.getValue().getStatus());
    }

    @Test
    void cardTokenIsSentToTheGatewayNotACardNumber() {
        answer(new Outcome(Result.APPROVED, "1", "3704", null));

        paymentService.pay(request());

        ArgumentCaptor<Charge> charge = ArgumentCaptor.forClass(Charge.class);
        verify(gateway).charge(charge.capture());
        assertEquals("tok_from_mercadopago", charge.getValue().cardToken());
        assertEquals("visa", charge.getValue().paymentMethodId());
    }

    @Test
    void rejectedPaymentIsNotApproved() {
        answer(new Outcome(Result.REJECTED, "2", "3704", "Fondos insuficientes"));

        PaymentDto result = paymentService.pay(request());

        assertEquals(PaymentStatus.REJECTED, result.status());
        assertEquals("Fondos insuficientes", result.message());
    }

    @Test
    void paymentUnderReviewStaysPending() {
        answer(new Outcome(Result.PENDING, "3", "3704", "El pago quedó en revisión en Mercado Pago"));

        assertEquals(PaymentStatus.PENDING, paymentService.pay(request()).status());
    }

    @Test
    void noResponseLeavesPaymentPending() {
        answer(new Outcome(Result.NO_RESPONSE, null, null, "El servicio de pagos no respondió a tiempo"));

        PaymentDto result = paymentService.pay(request());

        assertEquals(PaymentStatus.PENDING, result.status());
        assertNull(result.externalReference());
    }

    @Test
    void serviceErrorLeavesPaymentPending() {
        answer(new Outcome(Result.ERROR, null, null, "El servicio de pagos devolvió un error"));

        assertEquals(PaymentStatus.PENDING, paymentService.pay(request()).status());
    }

    private void answer(Outcome outcome) {
        when(gateway.charge(any(Charge.class))).thenReturn(outcome);
    }

    private static PaymentRequestDto request() {
        return new PaymentRequestDto(new BigDecimal("2100"), "tok_from_mercadopago", "visa", null,
                "test@testuser.com", "CI", "12345678");
    }
}
