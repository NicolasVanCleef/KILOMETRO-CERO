package com.uruguaymnatural.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.MyReviewDto;
import com.uruguaymnatural.backend.dto.ReviewDto;
import com.uruguaymnatural.backend.dto.ReviewRequestDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.BookingStatus;
import com.uruguaymnatural.backend.model.Review;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.repository.BookingRepository;
import com.uruguaymnatural.backend.repository.ReviewRepository;

/**
 * Reviews of activities (CU-20) and the provider's replies (CU-21).
 * They are published right away, without moderation (RN-34).
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final TouristService touristService;
    private final PortalService portalService;
    private final ActivityService activityService;

    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository,
            TouristService touristService, PortalService portalService, ActivityService activityService) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.touristService = touristService;
        this.portalService = portalService;
        this.activityService = activityService;
    }

    // ---- The tourist (CU-20) ----

    /** Whether the logged-in tourist can review the activity, and their review if they wrote one. */
    @Transactional(readOnly = true)
    public MyReviewDto getOwn(Long userId, Long activityId) {
        Tourist tourist = touristService.findByUser(userId);
        ReviewDto review = reviewRepository.findByActivityIdAndTouristId(activityId, tourist.getId())
                .map(ReviewDto::from)
                .orElse(null);
        return new MyReviewDto(participated(tourist, activityId), review);
    }

    @Transactional
    public ReviewDto create(Long userId, Long activityId, ReviewRequestDto data) {
        Tourist tourist = touristService.findByUser(userId);
        Activity activity = portalService.findPublished(activityId);
        if (!participated(tourist, activityId)) {
            throw new BusinessRuleException("Solo podés valorar una actividad de la que ya participaste.");
        }
        if (reviewRepository.findByActivityIdAndTouristId(activityId, tourist.getId()).isPresent()) {
            throw new BusinessRuleException("Ya valoraste esta actividad. Podés editar tu valoración.");
        }
        Review review = new Review(activity, tourist, data.rating(), data.comment().trim());
        return ReviewDto.from(reviewRepository.save(review));
    }

    @Transactional
    public ReviewDto update(Long userId, Long reviewId, ReviewRequestDto data) {
        Review review = findOwn(userId, reviewId);
        review.update(data.rating(), data.comment().trim());
        return ReviewDto.from(review);
    }

    @Transactional
    public void delete(Long userId, Long reviewId) {
        reviewRepository.delete(findOwn(userId, reviewId));
    }

    // ---- The provider (CU-21) ----

    /** The reviews of one of the logged-in provider's activities. */
    @Transactional(readOnly = true)
    public List<ReviewDto> listOfOwnActivity(Long userId, Long activityId) {
        activityService.findOwn(userId, activityId);
        return listOf(activityId);
    }

    /** The provider answers a comment of one of their activities. Answering again replaces the reply (RN-35). */
    @Transactional
    public ReviewDto reply(Long userId, Long reviewId, String reply) {
        Review review = find(reviewId);
        if (!review.getActivity().getBusiness().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Este comentario es de una actividad de otro emprendimiento.");
        }
        review.setProviderReply(reply.trim());
        return ReviewDto.from(review);
    }

    // ---- Shared ----

    /** The reviews of an activity, newest first (RF-05). */
    public List<ReviewDto> listOf(Long activityId) {
        return reviewRepository.findByActivityIdOrderByCreatedAtDesc(activityId).stream()
                .map(ReviewDto::from)
                .toList();
    }

    /**
     * RN-31: the tourist took part in the activity if they have a confirmed booking of a
     * date that already happened.
     */
    private boolean participated(Tourist tourist, Long activityId) {
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository
                .findByTouristIdAndScheduleActivityIdAndStatus(tourist.getId(), activityId, BookingStatus.CONFIRMED)
                .stream()
                .anyMatch(booking -> booking.getSchedule().getStart().isBefore(now));
    }

    /** RN-34: only the tourist who wrote a review can edit or delete it. */
    private Review findOwn(Long userId, Long reviewId) {
        Review review = find(reviewId);
        if (!review.getTourist().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Esta valoración es de otro usuario.");
        }
        return review;
    }

    private Review find(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la valoración " + reviewId + "."));
    }
}
