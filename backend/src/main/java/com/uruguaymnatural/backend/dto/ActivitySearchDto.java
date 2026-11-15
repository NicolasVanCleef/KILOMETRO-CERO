package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The portal's search filters (RF-02). Every filter is optional: null means "any".
 *
 * @param department exact department
 * @param locality   text contained in the locality
 * @param tagId      type of activity
 * @param date       activities with a date that day
 * @param maxPrice   activities that cost this or less per person
 * @param keyword    text contained in the name or the description
 */
public record ActivitySearchDto(
        String department,
        String locality,
        Long tagId,
        LocalDate date,
        BigDecimal maxPrice,
        String keyword) {
}
