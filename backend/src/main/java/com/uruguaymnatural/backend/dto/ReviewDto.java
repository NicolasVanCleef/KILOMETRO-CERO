package com.uruguaymnatural.backend.dto;

import java.time.LocalDateTime;

import com.uruguaymnatural.backend.model.Review;

/**
 * A review as it is shown in the detail of an activity (RF-05).
 *
 * @param touristName first name and initial of the last name, for example "Sofía M."
 */
public record ReviewDto(
        Long id,
        Long activityId,
        String touristName,
        int rating,
        String comment,
        String providerReply,
        LocalDateTime createdAt) {

    public static ReviewDto from(Review review) {
        String lastName = review.getTourist().getLastName();
        return new ReviewDto(
                review.getId(),
                review.getActivity().getId(),
                review.getTourist().getFirstName() + " " + lastName.charAt(0) + ".",
                review.getRating(),
                review.getComment(),
                review.getProviderReply(),
                review.getCreatedAt());
    }
}
