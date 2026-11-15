package com.uruguaymnatural.backend.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.uruguaymnatural.backend.model.Booking;
import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;
import com.uruguaymnatural.backend.repository.PaymentRepository;

/**
 * Gives money back when a paid booking is cancelled (RN-26 to RN-28).
 */
@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient gateway;

    public RefundService(PaymentRepository paymentRepository, PaymentGatewayClient gateway) {
        this.paymentRepository = paymentRepository;
        this.gateway = gateway;
    }

    /**
     * Refunds a percentage of what was paid for the booking: the activity's percentage when
     * the tourist cancels, 100 when the provider or the administrator cancels.
     * Returns the money given back (zero if the booking had no approved payment).
     */
    public BigDecimal refund(Booking booking, int percentage) {
        Payment payment = paymentRepository
                .findFirstByBookingIdAndStatus(booking.getId(), PaymentStatus.APPROVED)
                .orElse(null);
        if (payment == null || percentage <= 0) {
            return BigDecimal.ZERO;
        }
        return refund(payment, BookingService.percentageOf(payment.getAmount(), percentage));
    }

    /** Asks the payment service to give the money back and records it in the payment. */
    public BigDecimal refund(Payment payment, BigDecimal amount) {
        if (gateway.refund(payment.getExternalReference(), amount.longValue())) {
            payment.markRefunded(amount);
            return amount;
        }
        // The payment service failed: the payment is left as it was, with a note, so the
        // refund can be done by hand. The cancellation itself is not undone.
        payment.setMessage("Reembolso de $ " + amount.toPlainString() + " pendiente: el servicio de pagos no respondió");
        log.warn("Reembolso pendiente del pago {} por $ {}", payment.getId(), amount);
        return BigDecimal.ZERO;
    }
}
