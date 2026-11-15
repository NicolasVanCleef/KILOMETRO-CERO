package com.uruguaymnatural.backend.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Booking;
import com.uruguaymnatural.backend.model.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByTouristIdOrderByCreatedAtDesc(Long touristId);

    List<Booking> findByScheduleIdOrderByCreatedAtAsc(Long scheduleId);

    List<Booking> findByScheduleIdAndStatusIn(Long scheduleId, Collection<BookingStatus> statuses);

    /** Pending bookings whose time to pay ran out. */
    List<Booking> findByStatusAndPaymentDeadlineBefore(BookingStatus status, LocalDateTime moment);

    boolean existsByCode(String code);
}
