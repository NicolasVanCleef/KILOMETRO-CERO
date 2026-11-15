package com.uruguaymnatural.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A tourist's review: a rating from 1 to 5 and a comment (RN-33).
 */
public record ReviewRequestDto(
        @NotNull(message = "Elegí un puntaje de 1 a 5.")
        @Min(value = 1, message = "El puntaje va de 1 a 5.")
        @Max(value = 5, message = "El puntaje va de 1 a 5.")
        Integer rating,

        @NotBlank(message = "Escribí un comentario.")
        @Size(max = 1000, message = "Máximo 1000 caracteres.")
        String comment) {
}
