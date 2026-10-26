package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Data of the payment form (RF-20, RF-21).
 */
public record PaymentRequestDto(
        @NotNull(message = "Ingresá el monto.")
        @DecimalMin(value = "1", message = "El monto tiene que ser mayor a 0.")
        @Digits(integer = 10, fraction = 2, message = "El monto no es válido.")
        BigDecimal amount,

        @NotBlank(message = "Ingresá el número de tarjeta.")
        @Pattern(regexp = "[0-9 ]{13,23}", message = "El número de tarjeta tiene que tener entre 13 y 19 dígitos.")
        String cardNumber,

        @NotBlank(message = "Ingresá el nombre que figura en la tarjeta.")
        String holder,

        @NotBlank(message = "Ingresá el vencimiento.")
        @Pattern(regexp = "(0[1-9]|1[0-2])/\\d{2}", message = "Usá el formato MM/AA.")
        String expiry,

        @NotBlank(message = "Ingresá el código de seguridad.")
        @Pattern(regexp = "\\d{3,4}", message = "Son 3 o 4 dígitos.")
        String cvc) {
}
