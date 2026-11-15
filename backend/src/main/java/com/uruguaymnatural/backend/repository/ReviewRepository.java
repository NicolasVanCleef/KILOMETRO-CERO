package com.uruguaymnatural.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByActivityIdOrderByCreatedAtDesc(Long activityId);

    Optional<Review> findByActivityIdAndTouristId(Long activityId, Long touristId);

    /** The reviews written in a period, for the "best rated of the month" of the public API. */
    List<Review> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
