package com.uruguaymnatural.backend.dto;

import com.uruguaymnatural.backend.model.PersonType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data sent by the provider's registration form (CU-01, RF-29).
 */
public record BusinessRegistrationDto(
        @NotNull(message = "Elegí si sos persona física o jurídica.")
        PersonType personType,

        @NotBlank(message = "Ingresá la CI o el RUT.")
        String document,

        @NotBlank(message = "Ingresá la razón social.")
        @Size(max = 200, message = "Máximo 200 caracteres.")
        String legalName,

        @NotBlank(message = "Ingresá un teléfono.")
        @Size(max = 30, message = "Máximo 30 caracteres.")
        String phone,

        @NotBlank(message = "Ingresá un correo electrónico.")
        @Email(message = "El correo no es válido.")
        String email,

        @Size(max = 200, message = "Máximo 200 caracteres.")
        String representativeName,

        @NotBlank(message = "Contá brevemente qué servicios ofrecés.")
        @Size(max = 2000, message = "Máximo 2000 caracteres.")
        String description,

        @NotBlank(message = "Elegí el departamento.")
        String department,

        @NotBlank(message = "Ingresá la localidad.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String locality,

        @Size(max = 200, message = "Máximo 200 caracteres.")
        String address,

        @NotBlank(message = "Ingresá una contraseña.")
        @Size(min = 8, max = 100, message = "La contraseña tiene que tener al menos 8 caracteres.")
        String password) {
}
