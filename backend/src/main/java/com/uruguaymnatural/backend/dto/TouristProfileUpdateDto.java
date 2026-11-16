package com.uruguaymnatural.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data a tourist can change in their profile (CU-06). The email cannot be changed.
 */
public record TouristProfileUpdateDto(
        @NotBlank(message = "Ingresá tu nombre.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String firstName,

        @NotBlank(message = "Ingresá tu apellido.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String lastName,

        @NotBlank(message = "Ingresá tu país.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String country,

        @NotNull(message = "Ingresá tu fecha de nacimiento.")
        @Past(message = "La fecha de nacimiento tiene que ser una fecha pasada.")
        LocalDate birthDate,

        @NotBlank(message = "Elegí tu idioma de preferencia.")
        @Size(max = 50, message = "Máximo 50 caracteres.")
        String preferredLanguage,

        @NotBlank(message = "Ingresá tu cédula.")
        @Pattern(regexp = "\\d{7,8}", message = "La cédula tiene que tener 7 u 8 dígitos, solo números.")
        String document) {
}
