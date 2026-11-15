package com.uruguaymnatural.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Payment;
import com.uruguaymnatural.backend.model.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** The payment that confirmed a booking (there is at most one approved per booking). */
    Optional<Payment> findFirstByBookingIdAndStatus(Long bookingId, PaymentStatus status);

    /** The last attempt to pay a booking, whatever its result. */
    Optional<Payment> findFirstByBookingIdOrderByCreatedAtDesc(Long bookingId);
}
