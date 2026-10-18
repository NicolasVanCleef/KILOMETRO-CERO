package com.uruguaymnatural.backend.dto;

import java.time.LocalDateTime;

import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.model.PersonType;

/**
 * Full business data shown to the administrator before approving or rejecting it.
 * Never includes the password.
 */
public record BusinessDetailDto(
        Long id,
        PersonType personType,
        String document,
        String legalName,
        String phone,
        String email,
        String representativeName,
        String description,
        String department,
        String locality,
        String address,
        BusinessStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt) {

    public static BusinessDetailDto from(Business business) {
        return new BusinessDetailDto(
                business.getId(),
                business.getPersonType(),
                business.getDocument(),
                business.getLegalName(),
                business.getPhone(),
                business.getEmail(),
                business.getRepresentativeName(),
                business.getDescription(),
                business.getDepartment(),
                business.getLocality(),
                business.getAddress(),
                business.getStatus(),
                business.getCreatedAt(),
                business.getReviewedAt());
    }
}
