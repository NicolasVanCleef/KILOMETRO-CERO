package com.uruguaymnatural.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data a provider can change about their business from their own space (CU-06, RF-33).
 * Person type, document and legal name are not here: the administrator approved those.
 */
public record BusinessProfileUpdateDto(
        @NotBlank(message = "Ingresá un teléfono.")
        @Pattern(regexp = "\\d{9}", message = "El teléfono tiene que tener 9 dígitos, solo números.")
        String phone,

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
        String address) {
}
