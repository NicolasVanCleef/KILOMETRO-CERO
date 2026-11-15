package com.uruguaymnatural.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Second step of "recover password" (CU-05): the code that came in the link and the new password.
 */
public record PasswordResetConfirmDto(
        @NotBlank(message = "Falta el código del enlace.")
        String token,

        @NotBlank(message = "Ingresá una contraseña.")
        @Size(min = 8, max = 100, message = "La contraseña tiene que tener al menos 8 caracteres.")
        String password) {
}
