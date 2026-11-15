package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.dto.ActivityDetailDto;
import com.uruguaymnatural.backend.dto.ActivitySearchDto;
import com.uruguaymnatural.backend.dto.ActivitySummaryDto;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.Review;
import com.uruguaymnatural.backend.model.ScheduleStatus;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;
import com.uruguaymnatural.backend.repository.ReviewRepository;

class PortalServiceTest {

    private ActivityRepository activityRepository;
    private ActivityScheduleRepository scheduleRepository;
    private ReviewRepository reviewRepository;
    private PortalService portalService;

    // A horse ride in Rocha ($ 1500, tag 2 "Cabalgata") with a date in 3 days.
    private final Activity ride = TestData.activity(1L, TestData.business(5L));
    private final ActivitySchedule rideDate = TestData.schedule(10L, ride, LocalDateTime.now().plusDays(3));

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        activityRepository = mock(ActivityRepository.class);
        scheduleRepository = mock(ActivityScheduleRepository.class);
        when(activityRepository.findPublished()).thenReturn(List.of(ride));
        when(scheduleRepository.findByStatusAndDateGreaterThanEqual(any(), any())).thenReturn(List.of(rideDate));
        portalService = new PortalService(activityRepository, scheduleRepository, reviewRepository);
    }

    @Test
    void withoutFiltersShowsEveryPublishedActivity() {
        List<ActivitySummaryDto> result = portalService.search(filters(null, null, null, null, null));

        assertEquals(1, result.size());
        assertEquals("Cabalgata por la sierra", result.get(0).name());
        assertEquals(rideDate.getDate(), result.get(0).nextDate());
        assertEquals(new BigDecimal("1500"), result.get(0).priceFrom());
    }

    @Test
    void filtersByDepartment() {
        assertEquals(1, portalService.search(filters("rocha", null, null, null, null)).size());
        assertTrue(portalService.search(filters("Salto", null, null, null, null)).isEmpty());
    }

    @Test
    void filtersByTag() {
        assertEquals(1, portalService.search(filters(null, 2L, null, null, null)).size());
        assertTrue(portalService.search(filters(null, 9L, null, null, null)).isEmpty());
    }

    @Test
    void filtersByDate() {
        assertEquals(1, portalService.search(filters(null, null, rideDate.getDate(), null, null)).size());
        assertTrue(portalService.search(filters(null, null, LocalDate.now().plusDays(20), null, null)).isEmpty());
    }

    @Test
    void filtersByMaximumPrice() {
        assertEquals(1, portalService.search(filters(null, null, null, new BigDecimal("1500"), null)).size());
        assertTrue(portalService.search(filters(null, null, null, new BigDecimal("1000"), null)).isEmpty());
    }

    @Test
    void filtersByKeywordInNameOrDescription() {
        assertEquals(1, portalService.search(filters(null, null, null, null, "SIERRA")).size());
        assertEquals(1, portalService.search(filters(null, null, null, null, "caballo")).size());
        assertTrue(portalService.search(filters(null, null, null, null, "kayak")).isEmpty());
    }

    @Test
    void detailShowsOnlyComingDates() {
        ActivitySchedule past = TestData.schedule(11L, ride, LocalDateTime.now().minusDays(1));
        when(activityRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(scheduleRepository.findByActivityIdAndStatus(1L, ScheduleStatus.SCHEDULED))
                .thenReturn(List.of(past, rideDate));

        ActivityDetailDto detail = portalService.getDetail(1L);

        assertEquals(1, detail.schedules().size());
        assertEquals(10L, detail.schedules().get(0).id());
        assertTrue(detail.schedules().get(0).bookable());
    }

    @Test
    void detailShowsTheReviewsAndTheirAverage() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(ride));
        when(reviewRepository.findByActivityIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(
                new Review(ride, TestData.tourist(3L), 5, "Excelente"),
                new Review(ride, TestData.tourist(4L), 4, "Muy buena")));

        ActivityDetailDto detail = portalService.getDetail(1L);

        assertEquals(2, detail.reviews().size());
        assertEquals("Sofía M.", detail.reviews().get(0).touristName());
        assertEquals(4.5, detail.averageRating());
    }

    @Test
    void deletedActivityIsNotFound() {
        ride.deactivate();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(ride));

        assertThrows(ResourceNotFoundException.class, () -> portalService.getDetail(1L));
    }

    @Test
    void bookingWindowClosesTheMinutesBeforeTheStartThatTheProviderSet() {
        // Bookings close 60 minutes before the start.
        ride.setBookingRules(10, new BigDecimal("1500"), 60, null);
        LocalDateTime start = rideDate.getStart();

        assertTrue(PortalService.isInsideBookingWindow(rideDate, start.minusMinutes(61)));
        assertFalse(PortalService.isInsideBookingWindow(rideDate, start.minusMinutes(59)));
    }

    @Test
    void withZeroMinutesItCanBeBookedUntilTheStart() {
        LocalDateTime start = rideDate.getStart();

        assertTrue(PortalService.isInsideBookingWindow(rideDate, start.minusMinutes(1)));
        assertFalse(PortalService.isInsideBookingWindow(rideDate, start.plusMinutes(1)));
    }

    private static ActivitySearchDto filters(String department, Long tagId, LocalDate date, BigDecimal maxPrice,
            String keyword) {
        return new ActivitySearchDto(department, null, tagId, date, maxPrice, keyword);
    }
}
