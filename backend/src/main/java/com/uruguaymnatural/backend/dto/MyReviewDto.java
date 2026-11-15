package com.uruguaymnatural.backend.dto;

/**
 * What the logged-in tourist can do about the reviews of one activity.
 *
 * @param participated whether they took part in the activity, so they can review it (RN-31)
 * @param review       their own review, or null if they have not written one
 */
public record MyReviewDto(boolean participated, ReviewDto review) {
}
