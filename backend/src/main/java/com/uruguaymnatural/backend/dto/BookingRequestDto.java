package com.uruguaymnatural.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * First step of a booking (CU-15): which date and how many people.
 */
public record BookingRequestDto(
        @NotNull(message = "Elegí una fecha.")
        Long scheduleId,

        @NotNull(message = "Indicá cuántas personas van.")
        @Min(value = 1, message = "Tiene que ir al menos 1 persona.")
        Integer participantCount) {
}
