package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Data of the payment form (RF-20, RF-21).
 *
 * There is no card number here: Mercado Pago captures it in the browser and sends us only
 * a one-time token (cardToken).
 */
public record PaymentRequestDto(
        @NotNull(message = "Ingresá el monto.")
        @DecimalMin(value = "1", message = "El monto tiene que ser mayor a 0.")
        @Digits(integer = 10, fraction = 2, message = "El monto no es válido.")
        BigDecimal amount,

        @NotBlank(message = "Falta el token de la tarjeta.")
        String cardToken,

        @NotBlank(message = "No se reconoció el tipo de tarjeta.")
        String paymentMethodId,

        String issuerId,

        @NotBlank(message = "Ingresá un correo.")
        @Email(message = "El correo no es válido.")
        String payerEmail,

        @NotBlank(message = "Elegí el tipo de documento.")
        String identificationType,

        @NotBlank(message = "Ingresá el número de documento.")
        String identificationNumber) {
}
