package com.uruguaymnatural.backend.dto;

import java.time.LocalDate;

import com.uruguaymnatural.backend.model.Tourist;

/**
 * A tourist's profile as shown in "Mi perfil".
 */
public record TouristProfileDto(
        Long id,
        String email,
        String firstName,
        String lastName,
        String country,
        LocalDate birthDate,
        String preferredLanguage,
        String document) {

    public static TouristProfileDto from(Tourist tourist) {
        return new TouristProfileDto(
                tourist.getId(),
                tourist.getUser().getEmail(),
                tourist.getFirstName(),
                tourist.getLastName(),
                tourist.getCountry(),
                tourist.getBirthDate(),
                tourist.getPreferredLanguage(),
                tourist.getDocument());
    }
}
