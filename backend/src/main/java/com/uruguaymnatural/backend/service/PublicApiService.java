package com.uruguaymnatural.backend.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.ActivitySearchDto;
import com.uruguaymnatural.backend.dto.ActivitySummaryDto;
import com.uruguaymnatural.backend.dto.PublicApiDto.BusinessItem;
import com.uruguaymnatural.backend.dto.PublicApiDto.DayActivity;
import com.uruguaymnatural.backend.dto.PublicApiDto.DepartmentTop;
import com.uruguaymnatural.backend.dto.PublicApiDto.RatedActivity;
import com.uruguaymnatural.backend.dto.PublicApiDto.Verification;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.model.Review;
import com.uruguaymnatural.backend.model.ScheduleStatus;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;
import com.uruguaymnatural.backend.repository.BusinessRepository;
import com.uruguaymnatural.backend.repository.ReviewRepository;

/**
 * The public REST API for other platforms (CU-25). Lists and searches only show approved
 * businesses and their published activities (RN-38).
 */
@Service
public class PublicApiService {

    private static final int TOP_SIZE = 3;

    private final BusinessRepository businessRepository;
    private final ActivityScheduleRepository scheduleRepository;
    private final ReviewRepository reviewRepository;
    private final PortalService portalService;

    public PublicApiService(BusinessRepository businessRepository, ActivityScheduleRepository scheduleRepository,
            ReviewRepository reviewRepository, PortalService portalService) {
        this.businessRepository = businessRepository;
        this.scheduleRepository = scheduleRepository;
        this.reviewRepository = reviewRepository;
        this.portalService = portalService;
    }

    /** RF-48: every approved business. */
    @Transactional(readOnly = true)
    public List<BusinessItem> listBusinesses() {
        return businessRepository.findByStatusOrderByCreatedAtAsc(BusinessStatus.APPROVED).stream()
                .map(business -> new BusinessItem(business.getId(), business.getLegalName(),
                        business.getDescription(), business.getDepartment(), business.getLocality(),
                        business.getPhone(), business.getEmail()))
                .toList();
    }

    /** RF-49: the activities that take place on a day in a department and, optionally, a locality. */
    @Transactional(readOnly = true)
    public List<DayActivity> listActivitiesOfDay(LocalDate date, String department, String locality) {
        return scheduleRepository.findByDateAndStatusOrderByStartTimeAsc(date, ScheduleStatus.SCHEDULED).stream()
                .filter(schedule -> PortalService.isPublished(schedule.getActivity()))
                .filter(schedule -> schedule.getActivity().getDepartment().equalsIgnoreCase(department.trim()))
                .filter(schedule -> locality == null || locality.isBlank()
                        || schedule.getActivity().getLocality().toLowerCase().contains(locality.trim().toLowerCase()))
                .map(schedule -> {
                    Activity activity = schedule.getActivity();
                    return new DayActivity(activity.getId(), activity.getName(),
                            activity.getBusiness().getLegalName(), activity.getDepartment(), activity.getLocality(),
                            schedule.getDate(), schedule.getStartTime(), schedule.getPrice(),
                            schedule.getAvailableCapacity());
                })
                .toList();
    }

    /**
     * RF-50: the 3 best rated activities of the month in each department. The score of an
     * activity is the average of the ratings written that month (RN-37); with the same
     * average, the one with more reviews goes first.
     */
    @Transactional(readOnly = true)
    public List<DepartmentTop> topRatedOfMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new InvalidDataException("month", "El mes va de 1 a 12.");
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        List<Review> reviews = reviewRepository.findByCreatedAtBetween(
                yearMonth.atDay(1).atStartOfDay(), yearMonth.plusMonths(1).atDay(1).atStartOfDay());

        // Reviews grouped by activity, keeping only published activities.
        Map<Activity, List<Review>> byActivity = reviews.stream()
                .filter(review -> PortalService.isPublished(review.getActivity()))
                .collect(Collectors.groupingBy(Review::getActivity));

        // One scored activity per group, then grouped by department (in alphabetical order).
        Map<String, List<RatedActivity>> byDepartment = new TreeMap<>();
        byActivity.forEach((activity, activityReviews) -> byDepartment
                .computeIfAbsent(activity.getDepartment(), department -> new java.util.ArrayList<>())
                .add(new RatedActivity(activity.getId(), activity.getName(), activity.getBusiness().getLegalName(),
                        PortalService.averageRating(activityReviews), activityReviews.size())));

        return byDepartment.entrySet().stream()
                .map(entry -> new DepartmentTop(entry.getKey(), entry.getValue().stream()
                        .sorted(Comparator.comparingDouble(RatedActivity::averageRating).reversed()
                                .thenComparing(Comparator.comparingInt(RatedActivity::reviewCount).reversed()))
                        .limit(TOP_SIZE)
                        .toList()))
                .toList();
    }

    /** RF-51: is the business with this id approved by Uruguay Mnatural? */
    @Transactional(readOnly = true)
    public Verification verifyById(Long id) {
        return toVerification(businessRepository.findById(id));
    }

    /** RF-51: is the business with this CI or RUT approved by Uruguay Mnatural? */
    @Transactional(readOnly = true)
    public Verification verifyByDocument(String document) {
        return toVerification(businessRepository.findFirstByDocument(document.replaceAll("[^0-9]", "")));
    }

    /** RF-52: published activities whose name or description contains the keyword. */
    @Transactional(readOnly = true)
    public List<ActivitySummaryDto> searchActivities(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new InvalidDataException("q", "Indicá la palabra clave a buscar.");
        }
        return portalService.search(new ActivitySearchDto(null, null, null, null, null, keyword));
    }

    /** The answer always says something, even when the business does not exist (CU-25, 3b). */
    private static Verification toVerification(Optional<Business> business) {
        return business
                .map(found -> new Verification(true, found.isApproved(), found.getStatus().name(), found.getId(),
                        found.getLegalName()))
                .orElse(new Verification(false, false, null, null, null));
    }
}
