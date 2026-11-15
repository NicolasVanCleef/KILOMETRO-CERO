package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A coming date of an activity as the portal shows it (RF-04).
 *
 * @param bookable false when it has no places left or it is outside the minutes the provider
 *                 set to book (RN-10)
 */
public record PublicScheduleDto(
        Long id,
        LocalDate date,
        LocalTime startTime,
        int availableCapacity,
        BigDecimal price,
        boolean bookable) {
}
