package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.uruguaymnatural.backend.model.BookingStatus;

/**
 * What the provider needs to organise one date (CU-22, RF-41): who booked and who comes.
 */
public record ScheduleBookingsDto(
        String activityName,
        ScheduleDto schedule,
        List<Item> bookings) {

    public record Item(
            Long id,
            String code,
            BookingStatus status,
            String touristName,
            String touristEmail,
            int participantCount,
            BigDecimal total,
            List<ParticipantDto> participants) {
    }
}
