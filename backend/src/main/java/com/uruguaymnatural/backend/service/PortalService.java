package com.uruguaymnatural.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.ActivityDetailDto;
import com.uruguaymnatural.backend.dto.ActivityDto;
import com.uruguaymnatural.backend.dto.ActivitySearchDto;
import com.uruguaymnatural.backend.dto.ActivitySummaryDto;
import com.uruguaymnatural.backend.dto.PublicScheduleDto;
import com.uruguaymnatural.backend.dto.ReviewDto;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.Review;
import com.uruguaymnatural.backend.model.ScheduleStatus;
import com.uruguaymnatural.backend.model.Tag;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;
import com.uruguaymnatural.backend.repository.ReviewRepository;

/**
 * The public portal: anyone can search activities and see their detail without an account
 * (CU-13, CU-14, RF-01). Only activities of approved businesses are shown.
 */
@Service
public class PortalService {

    private final ActivityRepository activityRepository;
    private final ActivityScheduleRepository scheduleRepository;
    private final ReviewRepository reviewRepository;

    public PortalService(ActivityRepository activityRepository, ActivityScheduleRepository scheduleRepository,
            ReviewRepository reviewRepository) {
        this.activityRepository = activityRepository;
        this.scheduleRepository = scheduleRepository;
        this.reviewRepository = reviewRepository;
    }

    /** The published activities that pass every filter that was filled in (RF-02). */
    @Transactional(readOnly = true)
    public List<ActivitySummaryDto> search(ActivitySearchDto filters) {
        Map<Long, List<ActivitySchedule>> comingByActivity = comingSchedules().stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getActivity().getId()));

        return activityRepository.findPublished().stream()
                .filter(activity -> matches(activity, comingByActivity.getOrDefault(activity.getId(), List.of()), filters))
                .map(activity -> toSummary(activity, comingByActivity.getOrDefault(activity.getId(), List.of())))
                .toList();
    }

    /** One published activity with its coming dates and its reviews (RF-03, RF-04, RF-05). */
    @Transactional(readOnly = true)
    public ActivityDetailDto getDetail(Long activityId) {
        Activity activity = findPublished(activityId);
        LocalDateTime now = LocalDateTime.now();
        List<PublicScheduleDto> schedules = scheduleRepository
                .findByActivityIdAndStatus(activityId, ScheduleStatus.SCHEDULED).stream()
                .filter(schedule -> schedule.getStart().isAfter(now))
                .sorted(Comparator.comparing(ActivitySchedule::getStart))
                .map(schedule -> toPublic(schedule, now))
                .toList();
        List<Review> reviews = reviewRepository.findByActivityIdOrderByCreatedAtDesc(activityId);
        return new ActivityDetailDto(ActivityDto.from(activity), schedules,
                reviews.stream().map(ReviewDto::from).toList(), averageRating(reviews));
    }

    /** Average of the ratings with one decimal (4.3), or null without reviews. */
    public static Double averageRating(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return null;
        }
        double average = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
        return Math.round(average * 10) / 10.0;
    }

    /** A published activity: it exists, it was not deleted and its business is approved. */
    public Activity findPublished(Long activityId) {
        return activityRepository.findById(activityId)
                .filter(PortalService::isPublished)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la actividad " + activityId + "."));
    }

    public static boolean isPublished(Activity activity) {
        return activity.isActive() && activity.getBusiness().isApproved();
    }

    /**
     * Can this date still be booked? It needs free places and to be inside the minutes
     * before the start that the provider set (RN-10).
     */
    public static boolean isInsideBookingWindow(ActivitySchedule schedule, LocalDateTime now) {
        Activity activity = schedule.getActivity();
        LocalDateTime start = schedule.getStart();
        boolean notTooLate = !now.isAfter(start.minusMinutes(activity.getMinBookingMinutes())) && now.isBefore(start);
        boolean notTooEarly = activity.getMaxBookingMinutes() == null
                || !now.isBefore(start.minusMinutes(activity.getMaxBookingMinutes()));
        return schedule.isScheduled() && notTooLate && notTooEarly;
    }

    /** Every date of any activity that is scheduled and has not started yet. */
    private List<ActivitySchedule> comingSchedules() {
        LocalDateTime now = LocalDateTime.now();
        return scheduleRepository.findByStatusAndDateGreaterThanEqual(ScheduleStatus.SCHEDULED, now.toLocalDate())
                .stream()
                .filter(schedule -> schedule.getStart().isAfter(now))
                .toList();
    }

    private static boolean matches(Activity activity, List<ActivitySchedule> coming, ActivitySearchDto filters) {
        if (hasText(filters.department()) && !activity.getDepartment().equalsIgnoreCase(filters.department().trim())) {
            return false;
        }
        if (hasText(filters.locality()) && !contains(activity.getLocality(), filters.locality())) {
            return false;
        }
        if (filters.tagId() != null
                && activity.getTags().stream().noneMatch(tag -> tag.getId().equals(filters.tagId()))) {
            return false;
        }
        if (hasText(filters.keyword())
                && !contains(activity.getName(), filters.keyword())
                && !contains(activity.getDescription(), filters.keyword())) {
            return false;
        }
        if (filters.date() != null
                && coming.stream().noneMatch(schedule -> schedule.getDate().equals(filters.date()))) {
            return false;
        }
        return filters.maxPrice() == null || lowestPrice(activity, coming).compareTo(filters.maxPrice()) <= 0;
    }

    private static ActivitySummaryDto toSummary(Activity activity, List<ActivitySchedule> coming) {
        return new ActivitySummaryDto(
                activity.getId(),
                activity.getName(),
                activity.getBusiness().getLegalName(),
                activity.getDepartment(),
                activity.getLocality(),
                activity.getDurationMinutes(),
                activity.getTags().stream().map(Tag::getName).toList(),
                lowestPrice(activity, coming),
                coming.stream().map(ActivitySchedule::getDate).min(Comparator.naturalOrder()).orElse(null));
    }

    private static PublicScheduleDto toPublic(ActivitySchedule schedule, LocalDateTime now) {
        boolean bookable = schedule.getAvailableCapacity() > 0 && isInsideBookingWindow(schedule, now);
        return new PublicScheduleDto(schedule.getId(), schedule.getDate(), schedule.getStartTime(),
                schedule.getAvailableCapacity(), schedule.getPrice(), bookable);
    }

    /** The cheapest of the coming dates; without dates, the activity's default price. */
    private static BigDecimal lowestPrice(Activity activity, List<ActivitySchedule> coming) {
        return coming.stream().map(ActivitySchedule::getPrice).min(Comparator.naturalOrder())
                .orElse(activity.getDefaultPrice());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static boolean contains(String text, String part) {
        return text != null && text.toLowerCase().contains(part.trim().toLowerCase());
    }
}
