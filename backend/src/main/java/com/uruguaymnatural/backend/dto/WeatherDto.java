package com.uruguaymnatural.backend.dto;

import java.time.LocalDate;

/**
 * The weather forecast for one day at the place of an activity (RF-06).
 *
 * @param description              the sky in words, for example "Lluvia"
 * @param rainProbability percentage from 0 to 100; null if the service does not give it
 */
public record WeatherDto(
        LocalDate date,
        String description,
        double minTemperature,
        double maxTemperature,
        Integer rainProbability) {
}
