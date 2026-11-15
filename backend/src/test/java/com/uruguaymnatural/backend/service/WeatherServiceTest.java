package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.dto.WeatherDto;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ScheduleStatus;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;

class WeatherServiceTest {

    private ActivityScheduleRepository scheduleRepository;
    private WeatherClient weatherClient;
    private WeatherService weatherService;

    private final Activity activity = TestData.activity(1L, TestData.business(5L));   // in Rocha, no coordinates
    private final LocalDate inThreeDays = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(ActivityScheduleRepository.class);
        weatherClient = mock(WeatherClient.class);
        PortalService portalService = mock(PortalService.class);
        when(portalService.findPublished(1L)).thenReturn(activity);
        weatherService = new WeatherService(portalService, scheduleRepository, weatherClient);
    }

    @Test
    void returnsTheForecastOfTheDaysThatHaveADate() {
        hasDates(inThreeDays.atTime(10, 0), inThreeDays.plusDays(2).atTime(10, 0));
        when(weatherClient.dailyForecast(anyDouble(), anyDouble(), any(), any())).thenReturn(List.of(
                new WeatherDto(inThreeDays, "Despejado", 14, 25, 0),
                new WeatherDto(inThreeDays.plusDays(1), "Lluvia", 15, 20, 80),      // no date that day
                new WeatherDto(inThreeDays.plusDays(2), "Nublado", 13, 22, 20)));

        List<WeatherDto> forecast = weatherService.forecastFor(1L);

        assertEquals(List.of("Despejado", "Nublado"), forecast.stream().map(WeatherDto::description).toList());
    }

    @Test
    void withoutCoordinatesItAsksForTheCapitalOfTheDepartment() {
        hasDates(inThreeDays.atTime(10, 0));

        weatherService.forecastFor(1L);

        // Rocha's capital
        verify(weatherClient).dailyForecast(eq(-34.48), eq(-54.33), eq(inThreeDays), eq(inThreeDays));
    }

    @Test
    void withCoordinatesItAsksForThePlaceOfTheActivity() {
        activity.setLocation("Rocha", "La Paloma", null, -34.66, -54.16);
        hasDates(inThreeDays.atTime(10, 0));

        weatherService.forecastFor(1L);

        verify(weatherClient).dailyForecast(eq(-34.66), eq(-54.16), any(), any());
    }

    @Test
    void datesTooFarAheadDoNotAskTheWeatherService() {
        hasDates(LocalDateTime.now().plusDays(40));

        assertTrue(weatherService.forecastFor(1L).isEmpty());
        verify(weatherClient, never()).dailyForecast(anyDouble(), anyDouble(), any(), any());
    }

    @Test
    void ifTheWeatherServiceFailsThereIsSimplyNoForecast() {
        hasDates(inThreeDays.atTime(10, 0));
        // The client answers an empty list when the service fails or does not answer (RNF-14).
        when(weatherClient.dailyForecast(anyDouble(), anyDouble(), any(), any())).thenReturn(List.of());

        assertTrue(weatherService.forecastFor(1L).isEmpty());
    }

    @Test
    void weatherCodesAreDescribedInWords() {
        assertEquals("Despejado", WeatherClient.describe(0));
        assertEquals("Nublado", WeatherClient.describe(3));
        assertEquals("Lluvia", WeatherClient.describe(63));
        assertEquals("Tormenta", WeatherClient.describe(95));
    }

    private void hasDates(LocalDateTime... starts) {
        when(scheduleRepository.findByActivityIdAndStatus(1L, ScheduleStatus.SCHEDULED)).thenReturn(
                java.util.Arrays.stream(starts).map(start -> TestData.schedule(10L, activity, start)).toList());
    }
}
