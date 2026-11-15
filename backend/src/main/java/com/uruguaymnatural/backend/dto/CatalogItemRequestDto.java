package com.uruguaymnatural.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data to create or rename a tag or a type of equipment (CU-08, CU-09).
 */
public record CatalogItemRequestDto(
        @NotBlank(message = "Ingresá un nombre.")
        @Size(max = 60, message = "Máximo 60 caracteres.")
        String name) {
}
