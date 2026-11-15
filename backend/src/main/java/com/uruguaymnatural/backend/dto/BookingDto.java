package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.uruguaymnatural.backend.model.BookingStatus;

/**
 * A booking as the tourist sees it (CU-18).
 *
 * @param paymentDeadline while it is pending: until when the places are held
 * @param canCancel       whether the tourist can cancel it right now (RN-26)
 * @param cancelNote      why it cannot be cancelled, or what happens with the money if it is
 * @param refundAmount    money given back if it is cancelled right now
 * @param paymentMessage  why the last payment attempt did not go through, if that happened
 * @param canReview       the tourist took part (confirmed booking of a past date), so they can review it
 */
public record BookingDto(
        Long id,
        String code,
        BookingStatus status,
        Long activityId,
        String activityName,
        String businessName,
        LocalDate date,
        LocalTime startTime,
        int participantCount,
        BigDecimal unitPrice,
        BigDecimal total,
        LocalDateTime createdAt,
        LocalDateTime paymentDeadline,
        List<ParticipantDto> participants,
        boolean canCancel,
        String cancelNote,
        BigDecimal refundAmount,
        String paymentMessage,
        boolean canReview) {
}
