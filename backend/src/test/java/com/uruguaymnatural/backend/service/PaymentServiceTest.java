package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.uruguaymnatural.backend.dto.PaymentDto;
import com.uruguaymnatural.backend.dto.PaymentRequestDto;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;
import com.uruguaymnatural.backend.repository.PaymentRepository;
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
    void approvedPaymentIsSavedWithReferenceAndOnlyLast4() {
        answer(new Outcome(Result.APPROVED, "sbx_123", "tok_abc", null));

        PaymentDto result = paymentService.pay(request("4111 1111 1111 1111"));

        assertEquals(PaymentStatus.APPROVED, result.status());
        assertEquals("1111", result.cardLast4());
        assertEquals("sbx_123", result.externalReference());
        Payment saved = savedPayment();
        assertEquals("1111", saved.getCardLast4());
        assertEquals("tok_abc", saved.getCardToken());
        // the service gets the number without spaces
        verify(gateway).charge(eq("4111111111111111"), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void rejectedPaymentIsNotApproved() {
        answer(new Outcome(Result.REJECTED, null, null, "Fondos insuficientes"));

        PaymentDto result = paymentService.pay(request("4000000000000002"));

        assertEquals(PaymentStatus.REJECTED, result.status());
        assertEquals("Fondos insuficientes", result.message());
        assertNull(result.externalReference());
    }

    @Test
    void noResponseLeavesPaymentPending() {
        answer(new Outcome(Result.NO_RESPONSE, null, null, "El servicio de pagos no respondió a tiempo"));

        PaymentDto result = paymentService.pay(request("4000000000000408"));

        assertEquals(PaymentStatus.PENDING, result.status());
        assertEquals("El servicio de pagos no respondió a tiempo", result.message());
    }

    @Test
    void serviceErrorLeavesPaymentPending() {
        answer(new Outcome(Result.ERROR, null, null, "El servicio de pagos devolvió un error"));

        assertEquals(PaymentStatus.PENDING, paymentService.pay(request("4000000000000500")).status());
    }

    @Test
    void expiredCardIsRejectedBeforeCallingTheService() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String expiry = String.format("%02d/%02d", lastMonth.getMonthValue(), lastMonth.getYear() % 100);
        PaymentRequestDto data = new PaymentRequestDto(new BigDecimal("2100"), "4111111111111111", "Sofía Martínez",
                expiry, "123");

        InvalidDataException e = assertThrows(InvalidDataException.class, () -> paymentService.pay(data));
        assertEquals("expiry", e.getField());
        verify(gateway, never()).charge(anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    private void answer(Outcome outcome) {
        when(gateway.charge(anyString(), anyString(), anyString(), anyString(), anyLong())).thenReturn(outcome);
    }

    private Payment savedPayment() {
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        return captor.getValue();
    }

    private static PaymentRequestDto request(String cardNumber) {
        YearMonth next = YearMonth.now().plusYears(2);
        String expiry = String.format("%02d/%02d", next.getMonthValue(), next.getYear() % 100);
        return new PaymentRequestDto(new BigDecimal("2100"), cardNumber, "Sofía Martínez", expiry, "123");
    }
}
