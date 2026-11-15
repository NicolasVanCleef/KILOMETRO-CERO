package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.dto.PublicApiDto.DayActivity;
import com.uruguaymnatural.backend.dto.PublicApiDto.DepartmentTop;
import com.uruguaymnatural.backend.dto.PublicApiDto.Verification;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.model.PersonType;
import com.uruguaymnatural.backend.model.Review;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.ScheduleStatus;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;
import com.uruguaymnatural.backend.repository.BusinessRepository;
import com.uruguaymnatural.backend.repository.ReviewRepository;

class PublicApiServiceTest {

    private BusinessRepository businessRepository;
    private ActivityScheduleRepository scheduleRepository;
    private ReviewRepository reviewRepository;
    private PublicApiService publicApiService;

    private final Business business = TestData.business(5L);

    @BeforeEach
    void setUp() {
        businessRepository = mock(BusinessRepository.class);
        scheduleRepository = mock(ActivityScheduleRepository.class);
        reviewRepository = mock(ReviewRepository.class);
        publicApiService = new PublicApiService(businessRepository, scheduleRepository, reviewRepository,
                mock(PortalService.class));
    }

    @Test
    void listsOnlyApprovedBusinesses() {
        when(businessRepository.findByStatusOrderByCreatedAtAsc(BusinessStatus.APPROVED)).thenReturn(List.of(business));

        assertEquals("Cabalgatas Ana", publicApiService.listBusinesses().get(0).legalName());
    }

    @Test
    void activitiesOfADayAreFilteredByDepartmentAndLocality() {
        Activity ride = TestData.activity(1L, business);   // Rocha, La Paloma
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        ActivitySchedule date = TestData.schedule(10L, ride, start);
        when(scheduleRepository.findByDateAndStatusOrderByStartTimeAsc(start.toLocalDate(), ScheduleStatus.SCHEDULED))
                .thenReturn(List.of(date));

        List<DayActivity> inRocha = publicApiService.listActivitiesOfDay(start.toLocalDate(), "rocha", null);
        assertEquals(1, inRocha.size());
        assertEquals("Cabalgata por la sierra", inRocha.get(0).name());
        assertEquals(10, inRocha.get(0).availableCapacity());

        assertEquals(1, publicApiService.listActivitiesOfDay(start.toLocalDate(), "Rocha", "paloma").size());
        assertTrue(publicApiService.listActivitiesOfDay(start.toLocalDate(), "Rocha", "Chuy").isEmpty());
        assertTrue(publicApiService.listActivitiesOfDay(start.toLocalDate(), "Salto", null).isEmpty());
    }

    @Test
    void deletedActivitiesAreNotListed() {
        Activity ride = TestData.activity(1L, business);
        ride.deactivate();
        LocalDate day = LocalDate.now().plusDays(3);
        when(scheduleRepository.findByDateAndStatusOrderByStartTimeAsc(any(), any()))
                .thenReturn(List.of(TestData.schedule(10L, ride, day.atTime(10, 0))));

        assertTrue(publicApiService.listActivitiesOfDay(day, "Rocha", null).isEmpty());
    }

    @Test
    void topThreeOfTheMonthByDepartmentOrderedByAverageRating() {
        // Four activities in Rocha with averages 5, 4.5, 3 and 2: the last one is left out.
        List<Review> reviews = new ArrayList<>();
        reviews.addAll(reviewsOf(TestData.activity(1L, business), 5, 4));   // 4.5
        reviews.addAll(reviewsOf(TestData.activity(2L, business), 5));      // 5.0
        reviews.addAll(reviewsOf(TestData.activity(3L, business), 2));      // 2.0
        reviews.addAll(reviewsOf(TestData.activity(4L, business), 3, 3));   // 3.0
        when(reviewRepository.findByCreatedAtBetween(any(), any())).thenReturn(reviews);

        List<DepartmentTop> top = publicApiService.topRatedOfMonth(2026, 11);

        assertEquals(1, top.size());
        assertEquals("Rocha", top.get(0).department());
        assertEquals(List.of(2L, 1L, 4L),
                top.get(0).activities().stream().map(activity -> activity.activityId()).toList());
        assertEquals(4.5, top.get(0).activities().get(1).averageRating());
        assertEquals(2, top.get(0).activities().get(1).reviewCount());
    }

    @Test
    void topAsksOnlyForTheReviewsOfThatMonth() {
        when(reviewRepository.findByCreatedAtBetween(LocalDateTime.of(2026, 12, 1, 0, 0),
                LocalDateTime.of(2027, 1, 1, 0, 0))).thenReturn(reviewsOf(TestData.activity(1L, business), 5));

        assertEquals(1, publicApiService.topRatedOfMonth(2026, 12).size());
        assertTrue(publicApiService.topRatedOfMonth(2026, 11).isEmpty());
        assertThrows(InvalidDataException.class, () -> publicApiService.topRatedOfMonth(2026, 13));
    }

    @Test
    void verifiesAnApprovedBusiness() {
        when(businessRepository.findById(50L)).thenReturn(Optional.of(business));

        Verification verification = publicApiService.verifyById(50L);

        assertTrue(verification.found());
        assertTrue(verification.approved());
        assertEquals("APPROVED", verification.status());
    }

    @Test
    void aPendingBusinessIsFoundButNotApproved() {
        User user = new User("nuevo@mail.com", "hash", Role.PROVIDER, false);
        Business pending = new Business(user, PersonType.COMPANY, "217208670014", "Bodega Nueva", "099123456",
                "nuevo@mail.com", "Ana", "Vinos", "Canelones", "Las Piedras", null);
        when(businessRepository.findFirstByDocument("217208670014")).thenReturn(Optional.of(pending));

        // The document can come with dots or dashes.
        Verification verification = publicApiService.verifyByDocument("21.720867-0014");

        assertTrue(verification.found());
        assertFalse(verification.approved());
        assertEquals("PENDING", verification.status());
    }

    @Test
    void anUnknownBusinessIsAnsweredAsNotFoundInsteadOfAnError() {
        when(businessRepository.findById(99L)).thenReturn(Optional.empty());

        Verification verification = publicApiService.verifyById(99L);

        assertFalse(verification.found());
        assertFalse(verification.approved());
        assertNull(verification.status());
    }

    @Test
    void keywordSearchNeedsAKeyword() {
        assertThrows(InvalidDataException.class, () -> publicApiService.searchActivities(" "));
    }

    private static List<Review> reviewsOf(Activity activity, int... ratings) {
        List<Review> reviews = new ArrayList<>();
        for (int rating : ratings) {
            reviews.add(new Review(activity, TestData.tourist(3L), rating, "Comentario"));
        }
        return reviews;
    }
}
