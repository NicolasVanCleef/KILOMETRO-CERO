package com.uruguaymnatural.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.EquipmentRelation;

/**
 * All the data of an activity (RF-03): shown in the provider's space and in the public detail.
 */
public record ActivityDto(
        Long id,
        Long businessId,
        String businessName,
        String name,
        String description,
        String requirements,
        String observations,
        String department,
        String locality,
        String address,
        Double latitude,
        Double longitude,
        int durationMinutes,
        int defaultCapacity,
        BigDecimal defaultPrice,
        int minBookingMinutes,
        Integer maxBookingMinutes,
        boolean cancellable,
        Integer cancellationMinutes,
        boolean refundable,
        Integer refundPercentage,
        List<CatalogItemDto> tags,
        List<CatalogItemDto> requiredEquipment,
        List<CatalogItemDto> providedEquipment) {

    public static ActivityDto from(Activity activity) {
        return new ActivityDto(
                activity.getId(),
                activity.getBusiness().getId(),
                activity.getBusiness().getLegalName(),
                activity.getName(),
                activity.getDescription(),
                activity.getRequirements(),
                activity.getObservations(),
                activity.getDepartment(),
                activity.getLocality(),
                activity.getAddress(),
                activity.getLatitude(),
                activity.getLongitude(),
                activity.getDurationMinutes(),
                activity.getDefaultCapacity(),
                activity.getDefaultPrice(),
                activity.getMinBookingMinutes(),
                activity.getMaxBookingMinutes(),
                activity.isCancellable(),
                activity.getCancellationMinutes(),
                activity.isRefundable(),
                activity.getRefundPercentage(),
                activity.getTags().stream().map(CatalogItemDto::from).toList(),
                equipment(activity, EquipmentRelation.REQUIRED),
                equipment(activity, EquipmentRelation.PROVIDED));
    }

    private static List<CatalogItemDto> equipment(Activity activity, EquipmentRelation relation) {
        return activity.getEquipment().stream()
                .filter(item -> item.getRelation() == relation)
                .map(item -> CatalogItemDto.from(item.getEquipment()))
                .toList();
    }
}
