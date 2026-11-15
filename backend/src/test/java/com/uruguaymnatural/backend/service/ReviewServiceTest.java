package com.uruguaymnatural.backend.service;

import static com.uruguaymnatural.backend.TestData.withId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.dto.MyReviewDto;
import com.uruguaymnatural.backend.dto.ReviewDto;
import com.uruguaymnatural.backend.dto.ReviewRequestDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.Booking;
import com.uruguaymnatural.backend.model.BookingStatus;
import com.uruguaymnatural.backend.model.Review;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.repository.BookingRepository;
import com.uruguaymnatural.backend.repository.ReviewRepository;

class ReviewServiceTest {

    private static final Long TOURIST_USER = 3L;
    private static final Long PROVIDER_USER = 5L;

    private ReviewRepository reviewRepository;
    private BookingRepository bookingRepository;
    private ReviewService reviewService;

    private final Tourist tourist = TestData.tourist(TOURIST_USER);
    private final Activity activity = TestData.activity(1L, TestData.business(PROVIDER_USER));

    @BeforeEach
    void setUp() {
        reviewRepository = mock(ReviewRepository.class);
        when(reviewRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        when(reviewRepository.findByActivityIdAndTouristId(any(), any())).thenReturn(Optional.empty());
        bookingRepository = mock(BookingRepository.class);
        TouristService touristService = mock(TouristService.class);
        when(touristService.findByUser(TOURIST_USER)).thenReturn(tourist);
        PortalService portalService = mock(PortalService.class);
        when(portalService.findPublished(1L)).thenReturn(activity);
        reviewService = new ReviewService(reviewRepository, bookingRepository, touristService, portalService,
                mock(ActivityService.class));
    }

    // ---- RN-31: only who took part can review ----

    @Test
    void touristWhoTookPartCanReview() {
        confirmedBookingThatStarted(LocalDateTime.now().minusDays(2));

        ReviewDto review = reviewService.create(TOURIST_USER, 1L, new ReviewRequestDto(5, " Excelente paseo "));

        assertEquals(5, review.rating());
        assertEquals("Excelente paseo", review.comment());
        assertEquals("Sofía M.", review.touristName());
        assertNull(review.providerReply());
    }

    @Test
    void touristWithoutABookingCannotReview() {
        when(bookingRepository.findByTouristIdAndScheduleActivityIdAndStatus(any(), any(), any()))
                .thenReturn(List.of());

        assertThrows(BusinessRuleException.class,
                () -> reviewService.create(TOURIST_USER, 1L, new ReviewRequestDto(5, "Excelente")));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void touristWhoseDateHasNotHappenedYetCannotReview() {
        confirmedBookingThatStarted(LocalDateTime.now().plusDays(2));

        assertThrows(BusinessRuleException.class,
                () -> reviewService.create(TOURIST_USER, 1L, new ReviewRequestDto(5, "Excelente")));
        assertFalse(reviewService.getOwn(TOURIST_USER, 1L).participated());
    }

    @Test
    void aTouristReviewsAnActivityOnlyOnce() {
        confirmedBookingThatStarted(LocalDateTime.now().minusDays(2));
        Review existing = withId(new Review(activity, tourist, 4, "Muy buena"), 30L);
        when(reviewRepository.findByActivityIdAndTouristId(1L, tourist.getId())).thenReturn(Optional.of(existing));

        assertThrows(BusinessRuleException.class,
                () -> reviewService.create(TOURIST_USER, 1L, new ReviewRequestDto(5, "Excelente")));

        MyReviewDto mine = reviewService.getOwn(TOURIST_USER, 1L);
        assertTrue(mine.participated());
        assertEquals(30L, mine.review().id());
    }

    // ---- RN-34: only the author edits or deletes ----

    @Test
    void theAuthorCanEditAndDeleteTheirReview() {
        Review review = storedReview();

        reviewService.update(TOURIST_USER, 30L, new ReviewRequestDto(3, "Estuvo bien"));
        assertEquals(3, review.getRating());
        assertEquals("Estuvo bien", review.getComment());

        reviewService.delete(TOURIST_USER, 30L);
        verify(reviewRepository).delete(review);
    }

    @Test
    void anotherTouristCannotEditOrDeleteIt() {
        storedReview();

        assertThrows(ForbiddenException.class,
                () -> reviewService.update(99L, 30L, new ReviewRequestDto(1, "Malo")));
        assertThrows(ForbiddenException.class, () -> reviewService.delete(99L, 30L));
    }

    // ---- RN-35: the provider answers the comments of their own activities ----

    @Test
    void theProviderRepliesToAReviewOfTheirActivity() {
        Review review = storedReview();

        ReviewDto result = reviewService.reply(PROVIDER_USER, 30L, " ¡Gracias por venir! ");

        assertEquals("¡Gracias por venir!", result.providerReply());
        assertEquals("¡Gracias por venir!", review.getProviderReply());
    }

    @Test
    void anotherProviderCannotReply() {
        storedReview();

        assertThrows(ForbiddenException.class, () -> reviewService.reply(8L, 30L, "Gracias"));
    }

    private Review storedReview() {
        Review review = withId(new Review(activity, tourist, 4, "Muy buena"), 30L);
        when(reviewRepository.findById(30L)).thenReturn(Optional.of(review));
        return review;
    }

    private void confirmedBookingThatStarted(LocalDateTime start) {
        Booking booking = new Booking(tourist, TestData.schedule(10L, activity, start), 1, "UM-TEST0001",
                LocalDateTime.now());
        booking.confirm();
        when(bookingRepository.findByTouristIdAndScheduleActivityIdAndStatus(tourist.getId(), 1L,
                BookingStatus.CONFIRMED)).thenReturn(List.of(booking));
    }
}
