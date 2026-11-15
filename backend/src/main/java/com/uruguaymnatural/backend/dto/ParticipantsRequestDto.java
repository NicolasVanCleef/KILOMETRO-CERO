package com.uruguaymnatural.backend.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

/**
 * Second step of a booking (CU-15): the data of every participant.
 */
public record ParticipantsRequestDto(
        @NotEmpty(message = "Cargá los datos de los participantes.")
        @Valid
        List<ParticipantDto> participants) {
}
