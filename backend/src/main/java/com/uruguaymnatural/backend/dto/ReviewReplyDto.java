package com.uruguaymnatural.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * The provider's answer to a comment (CU-21).
 */
public record ReviewReplyDto(
        @NotBlank(message = "Escribí la respuesta.")
        @Size(max = 1000, message = "Máximo 1000 caracteres.")
        String reply) {
}
