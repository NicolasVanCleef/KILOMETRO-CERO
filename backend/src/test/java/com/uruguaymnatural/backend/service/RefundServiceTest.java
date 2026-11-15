package com.uruguaymnatural.backend.service;

import static com.uruguaymnatural.backend.TestData.withId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.Booking;
import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;
import com.uruguaymnatural.backend.repository.PaymentRepository;

class RefundServiceTest {

    private PaymentGatewayClient gateway;
    private PaymentRepository paymentRepository;
    private RefundService refundService;

    // A confirmed booking paid with $ 3000.
    private final Activity activity = TestData.activity(1L, TestData.business(5L));
    private final Booking booking = withId(new Booking(TestData.tourist(3L),
            TestData.schedule(10L, activity, LocalDateTime.now().plusDays(3)), 2, "UM-TEST0001",
            LocalDateTime.now()), 20L);
    private final Payment payment = new Payment(booking, null, new BigDecimal("3000"), PaymentStatus.APPROVED,
            "1111", "sbx_123", null);

    @BeforeEach
    void setUp() {
        gateway = mock(PaymentGatewayClient.class);
        paymentRepository = mock(PaymentRepository.class);
        when(paymentRepository.findFirstByBookingIdAndStatus(20L, PaymentStatus.APPROVED))
                .thenReturn(Optional.of(payment));
        refundService = new RefundService(paymentRepository, gateway);
    }

    @Test
    void refundsThePercentageThroughThePaymentService() {
        when(gateway.refund("sbx_123", 1500L)).thenReturn(true);

        assertEquals(new BigDecimal("1500.00"), refundService.refund(booking, 50));

        assertEquals(PaymentStatus.REFUNDED, payment.getStatus());
        assertEquals(new BigDecimal("1500.00"), payment.getRefundedAmount());
    }

    @Test
    void fullRefundWhenTheProviderCancels() {
        when(gateway.refund("sbx_123", 3000L)).thenReturn(true);

        assertEquals(new BigDecimal("3000.00"), refundService.refund(booking, 100));
    }

    @Test
    void bookingWithoutApprovedPaymentHasNothingToRefund() {
        when(paymentRepository.findFirstByBookingIdAndStatus(20L, PaymentStatus.APPROVED))
                .thenReturn(Optional.empty());

        assertEquals(BigDecimal.ZERO, refundService.refund(booking, 100));
        verify(gateway, never()).refund(anyString(), anyLong());
    }

    @Test
    void ifThePaymentServiceFailsTheRefundStaysPending() {
        when(gateway.refund(anyString(), anyLong())).thenReturn(false);

        assertEquals(BigDecimal.ZERO, refundService.refund(booking, 100));

        assertEquals(PaymentStatus.APPROVED, payment.getStatus());
        assertEquals("Reembolso de $ 3000.00 pendiente: el servicio de pagos no respondió", payment.getMessage());
    }
}
