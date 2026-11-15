package com.uruguaymnatural.backend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.WeatherDto;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.Departments;
import com.uruguaymnatural.backend.model.ScheduleStatus;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;

/**
 * The weather forecast for the coming dates of an activity (RF-06, optional in the statement).
 */
@Service
public class WeatherService {

    /** Weather services only forecast about two weeks ahead. */
    private static final int FORECAST_DAYS = 15;

    private final PortalService portalService;
    private final ActivityScheduleRepository scheduleRepository;
    private final WeatherClient weatherClient;

    public WeatherService(PortalService portalService, ActivityScheduleRepository scheduleRepository,
            WeatherClient weatherClient) {
        this.portalService = portalService;
        this.scheduleRepository = scheduleRepository;
        this.weatherClient = weatherClient;
    }

    /**
     * The forecast of each coming date of the activity that is close enough to have one.
     * If the weather service does not answer the list is empty and the page shows without
     * the weather (RNF-14).
     */
    @Transactional(readOnly = true)
    public List<WeatherDto> forecastFor(Long activityId) {
        Activity activity = portalService.findPublished(activityId);
        LocalDateTime now = LocalDateTime.now();
        LocalDate lastDay = now.toLocalDate().plusDays(FORECAST_DAYS);

        Set<LocalDate> days = scheduleRepository.findByActivityIdAndStatus(activityId, ScheduleStatus.SCHEDULED)
                .stream()
                .filter(schedule -> schedule.getStart().isAfter(now))
                .map(ActivitySchedule::getDate)
                .filter(day -> !day.isAfter(lastDay))
                .collect(Collectors.toSet());
        if (days.isEmpty()) {
            return List.of();
        }

        // The activity's own coordinates if the provider loaded them; if not, its department's capital.
        double[] place = activity.getLatitude() != null && activity.getLongitude() != null
                ? new double[] { activity.getLatitude(), activity.getLongitude() }
                : Departments.coordinatesOf(activity.getDepartment());

        LocalDate first = days.stream().min(LocalDate::compareTo).orElseThrow();
        LocalDate last = days.stream().max(LocalDate::compareTo).orElseThrow();
        return weatherClient.dailyForecast(place[0], place[1], first, last).stream()
                .filter(day -> days.contains(day.date()))
                .toList();
    }
}
