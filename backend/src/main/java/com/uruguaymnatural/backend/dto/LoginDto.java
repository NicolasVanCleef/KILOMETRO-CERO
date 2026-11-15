package com.uruguaymnatural.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data of the login form (CU-04).
 */
public record LoginDto(
        @NotBlank(message = "Ingresá tu correo.")
        String email,

        @NotBlank(message = "Ingresá tu contraseña.")
        String password) {
}
