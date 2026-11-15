package com.uruguaymnatural.backend.dto;

import java.util.List;

/**
 * Everything the portal shows when a visitor opens an activity (CU-14).
 */
public record ActivityDetailDto(
        ActivityDto activity,
        List<PublicScheduleDto> schedules) {
}
