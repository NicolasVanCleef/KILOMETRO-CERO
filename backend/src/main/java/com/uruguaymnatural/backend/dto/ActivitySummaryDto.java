package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * One activity in the portal's list of results.
 *
 * @param priceFrom lowest price among its next dates (the default price if it has none)
 * @param nextDate  its next date that has not happened yet, or null
 */
public record ActivitySummaryDto(
        Long id,
        String name,
        String businessName,
        String department,
        String locality,
        int durationMinutes,
        List<String> tags,
        BigDecimal priceFrom,
        LocalDate nextDate) {
}
