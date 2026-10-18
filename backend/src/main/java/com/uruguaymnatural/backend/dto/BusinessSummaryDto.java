package com.uruguaymnatural.backend.dto;

import java.time.LocalDateTime;

import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.model.PersonType;

/**
 * One row of the administrator's business list.
 */
public record BusinessSummaryDto(
        Long id,
        String legalName,
        PersonType personType,
        String document,
        String department,
        String locality,
        BusinessStatus status,
        LocalDateTime createdAt) {

    public static BusinessSummaryDto from(Business business) {
        return new BusinessSummaryDto(
                business.getId(),
                business.getLegalName(),
                business.getPersonType(),
                business.getDocument(),
                business.getDepartment(),
                business.getLocality(),
                business.getStatus(),
                business.getCreatedAt());
    }
}
