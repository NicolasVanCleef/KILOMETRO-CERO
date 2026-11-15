package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * A new date of an activity (CU-11). Without capacity or price, the activity's defaults are used.
 */
public record ScheduleRequestDto(
        @NotNull(message = "Elegí la fecha.")
        LocalDate date,

        @NotNull(message = "Elegí la hora de inicio.")
        LocalTime startTime,

        @Min(value = 1, message = "El cupo tiene que ser de al menos 1 persona.")
        Integer capacity,

        @DecimalMin(value = "0", message = "El precio no puede ser negativo.")
        BigDecimal price) {
}
