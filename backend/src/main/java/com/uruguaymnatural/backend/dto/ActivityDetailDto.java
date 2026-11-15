package com.uruguaymnatural.backend.dto;

import java.util.List;

/**
 * Everything the portal shows when a visitor opens an activity (CU-14).
 *
 * @param averageRating average of the ratings, or null if nobody reviewed it yet
 */
public record ActivityDetailDto(
        ActivityDto activity,
        List<PublicScheduleDto> schedules,
        List<ReviewDto> reviews,
        Double averageRating) {
}
