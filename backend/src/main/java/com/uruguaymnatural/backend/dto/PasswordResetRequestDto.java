package com.uruguaymnatural.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * First step of "recover password" (CU-05): the email of the account.
 */
public record PasswordResetRequestDto(
        @NotBlank(message = "Ingresá tu correo.")
        @Email(message = "El correo no es válido.")
        String email) {
}
