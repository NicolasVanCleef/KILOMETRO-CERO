package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Answers of the public API that other platforms consume (RF-48 to RF-52).
 * They only carry public data: nothing about users, bookings or payments.
 */
public final class PublicApiDto {

    private PublicApiDto() {
    }

    /** An approved business (RF-48). */
    public record BusinessItem(
            Long id,
            String legalName,
            String description,
            String department,
            String locality,
            String phone,
            String email) {
    }

    /** One date of an activity on the day that was asked for (RF-49). */
    public record DayActivity(
            Long activityId,
            String name,
            String businessName,
            String department,
            String locality,
            LocalDate date,
            LocalTime startTime,
            BigDecimal price,
            int availableCapacity) {
    }

    /** An activity with the average of its ratings in the month (RF-50). */
    public record RatedActivity(
            Long activityId,
            String name,
            String businessName,
            double averageRating,
            int reviewCount) {
    }

    /** The best rated activities of one department in the month (RF-50). */
    public record DepartmentTop(String department, List<RatedActivity> activities) {
    }

    /**
     * Whether a business is approved by Uruguay Mnatural (RF-51).
     *
     * @param found    false when no business matches what was asked
     * @param approved true only for a business the administrator approved
     * @param status   PENDING, APPROVED or REJECTED; null when not found
     */
    public record Verification(boolean found, boolean approved, String status, Long id, String legalName) {
    }
}
