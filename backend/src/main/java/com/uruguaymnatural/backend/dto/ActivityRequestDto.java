package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data of the activity form, to create or modify an activity (CU-10, RF-35 to RF-37).
 */
public record ActivityRequestDto(
        @NotBlank(message = "Ingresá el nombre de la actividad.")
        @Size(max = 150, message = "Máximo 150 caracteres.")
        String name,

        @NotBlank(message = "Describí la actividad.")
        @Size(max = 2000, message = "Máximo 2000 caracteres.")
        String description,

        @Size(max = 1000, message = "Máximo 1000 caracteres.")
        String requirements,

        @Size(max = 1000, message = "Máximo 1000 caracteres.")
        String observations,

        @NotBlank(message = "Elegí el departamento.")
        String department,

        @NotBlank(message = "Ingresá la localidad.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String locality,

        @Size(max = 200, message = "Máximo 200 caracteres.")
        String address,

        @DecimalMin(value = "-90", message = "La latitud va de -90 a 90.")
        @DecimalMax(value = "90", message = "La latitud va de -90 a 90.")
        Double latitude,

        @DecimalMin(value = "-180", message = "La longitud va de -180 a 180.")
        @DecimalMax(value = "180", message = "La longitud va de -180 a 180.")
        Double longitude,

        @NotNull(message = "Ingresá la duración en minutos.")
        @Min(value = 1, message = "La duración tiene que ser de al menos 1 minuto.")
        Integer durationMinutes,

        @NotNull(message = "Ingresá el cupo.")
        @Min(value = 1, message = "El cupo tiene que ser de al menos 1 persona.")
        Integer defaultCapacity,

        @NotNull(message = "Ingresá el precio (0 si no tiene costo).")
        @DecimalMin(value = "0", message = "El precio no puede ser negativo.")
        BigDecimal defaultPrice,

        @NotNull(message = "Ingresá los minutos (0 = hasta la hora de inicio).")
        @Min(value = 0, message = "No puede ser negativo.")
        Integer minBookingMinutes,

        @Min(value = 1, message = "Tiene que ser mayor que 0.")
        Integer maxBookingMinutes,

        boolean cancellable,

        @Min(value = 0, message = "No puede ser negativo.")
        Integer cancellationMinutes,

        boolean refundable,

        Integer refundPercentage,

        @NotEmpty(message = "Elegí al menos una etiqueta.")
        List<Long> tagIds,

        List<Long> requiredEquipmentIds,

        List<Long> providedEquipmentIds) {
}
