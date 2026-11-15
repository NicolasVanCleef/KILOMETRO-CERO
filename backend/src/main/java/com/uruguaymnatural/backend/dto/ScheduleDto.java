package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.ScheduleStatus;

/**
 * A date of an activity with its places and price (RF-04).
 */
public record ScheduleDto(
        Long id,
        Long activityId,
        LocalDate date,
        LocalTime startTime,
        int totalCapacity,
        int availableCapacity,
        BigDecimal price,
        ScheduleStatus status) {

    public static ScheduleDto from(ActivitySchedule schedule) {
        return new ScheduleDto(
                schedule.getId(),
                schedule.getActivity().getId(),
                schedule.getDate(),
                schedule.getStartTime(),
                schedule.getTotalCapacity(),
                schedule.getAvailableCapacity(),
                schedule.getPrice(),
                schedule.getStatus());
    }
}
