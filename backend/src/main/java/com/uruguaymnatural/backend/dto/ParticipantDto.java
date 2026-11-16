package com.uruguaymnatural.backend.dto;

import java.time.LocalDate;

import com.uruguaymnatural.backend.model.BookingParticipant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * One person of a booking: name, last name, birth date and ID (RN-16).
 * Used both to receive the data and to show it.
 */
public record ParticipantDto(
        @NotBlank(message = "Ingresá el nombre.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String firstName,

        @NotBlank(message = "Ingresá el apellido.")
        @Size(max = 100, message = "Máximo 100 caracteres.")
        String lastName,

        @NotNull(message = "Ingresá la fecha de nacimiento.")
        @Past(message = "La fecha de nacimiento tiene que ser una fecha pasada.")
        LocalDate birthDate,

        @NotBlank(message = "Ingresá la cédula.")
        @Pattern(regexp = "\\d{7,8}", message = "La cédula tiene que tener 7 u 8 dígitos, solo números.")
        String document) {

    public static ParticipantDto from(BookingParticipant participant) {
        return new ParticipantDto(participant.getFirstName(), participant.getLastName(),
                participant.getBirthDate(), participant.getDocument());
    }
}
