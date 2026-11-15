package com.uruguaymnatural.backend.service;

import static com.uruguaymnatural.backend.TestData.withId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.dto.BookingDto;
import com.uruguaymnatural.backend.dto.BookingRequestDto;
import com.uruguaymnatural.backend.dto.ParticipantDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.Booking;
import com.uruguaymnatural.backend.model.BookingStatus;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;
import com.uruguaymnatural.backend.repository.BookingRepository;
import com.uruguaymnatural.backend.repository.PaymentRepository;

class BookingServiceTest {

    private static final Long TOURIST_USER = 3L;

    private BookingRepository bookingRepository;
    private ActivityScheduleRepository scheduleRepository;
    private EmailService emailService;
    private RefundService refundService;
    private BookingService bookingService;

    private final Tourist tourist = TestData.tourist(TOURIST_USER);
    // 10 places at $ 1500, cancellable until 24 h before with 50 % refund. The date is in 3 days.
    private final Activity activity = TestData.activity(1L, TestData.business(5L));
    private final ActivitySchedule schedule = TestData.schedule(10L, activity, LocalDateTime.now().plusDays(3));

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        when(bookingRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        scheduleRepository = mock(ActivityScheduleRepository.class);
        when(scheduleRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(schedule));
        TouristService touristService = mock(TouristService.class);
        when(touristService.findByUser(TOURIST_USER)).thenReturn(tourist);
        emailService = mock(EmailService.class);
        refundService = mock(RefundService.class);
        when(refundService.refund(any(Booking.class), anyInt())).thenReturn(new BigDecimal("3000.00"));
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        bookingService = new BookingService(bookingRepository, scheduleRepository, touristService, emailService,
                refundService, paymentRepository, 10);
    }

    // ---- Holding places (CU-15, RN-13 to RN-15) ----

    @Test
    void holdingPlacesTakesThemFromTheDateForTenMinutes() {
        BookingDto booking = bookingService.hold(TOURIST_USER, new BookingRequestDto(10L, 4));

        assertEquals(BookingStatus.PENDING_PAYMENT, booking.status());
        assertEquals(6, schedule.getAvailableCapacity());
        assertEquals(new BigDecimal("6000"), booking.total());
        assertTrue(booking.code().startsWith("UM-"));
        assertTrue(booking.paymentDeadline().isAfter(LocalDateTime.now().plusMinutes(9)));
        assertTrue(booking.paymentDeadline().isBefore(LocalDateTime.now().plusMinutes(11)));
    }

    @Test
    void cannotBookMorePlacesThanAreLeft() {
        bookingService.hold(TOURIST_USER, new BookingRequestDto(10L, 8));

        BusinessRuleException e = assertThrows(BusinessRuleException.class,
                () -> bookingService.hold(TOURIST_USER, new BookingRequestDto(10L, 3)));
        assertTrue(e.getMessage().contains("quedan 2 lugares"));
        assertEquals(2, schedule.getAvailableCapacity());
    }

    @Test
    void placesNeverGoBelowZero() {
        bookingService.hold(TOURIST_USER, new BookingRequestDto(10L, 10));

        assertThrows(BusinessRuleException.class, () -> bookingService.hold(TOURIST_USER, new BookingRequestDto(10L, 1)));
        assertEquals(0, schedule.getAvailableCapacity());
    }

    @Test
    void cannotBookAfterTheMinutesTheProviderSet() {
        // Bookings close 5 days before the start, and the date is in 3 days.
        activity.setBookingRules(10, new BigDecimal("1500"), 5 * 24 * 60, null);

        assertThrows(BusinessRuleException.class, () -> bookingService.hold(TOURIST_USER, new BookingRequestDto(10L, 1)));
        assertEquals(10, schedule.getAvailableCapacity());
    }

    @Test
    void cannotBookACancelledDate() {
        schedule.cancel();

        assertThrows(BusinessRuleException.class, () -> bookingService.hold(TOURIST_USER, new BookingRequestDto(10L, 1)));
    }

    // ---- Participants (RN-16) ----

    @Test
    void participantsMustBeAsManyAsThePlaces() {
        Booking booking = pendingBooking(2);

        InvalidDataException e = assertThrows(InvalidDataException.class,
                () -> bookingService.saveParticipants(TOURIST_USER, 20L, List.of(participant("Ana"))));
        assertEquals("participants", e.getField());
        assertFalse(booking.hasParticipants());
    }

    @Test
    void paidActivityStaysPendingAfterLoadingParticipants() {
        Booking booking = pendingBooking(2);

        BookingDto result = bookingService.saveParticipants(TOURIST_USER, 20L,
                List.of(participant("Ana"), participant("Luis")));

        assertEquals(BookingStatus.PENDING_PAYMENT, result.status());
        assertEquals(2, booking.getParticipants().size());
        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void freeActivityIsConfirmedWithoutPaymentAndTheCodeIsEmailed() {
        ActivitySchedule freeDate = withId(new ActivitySchedule(activity, schedule.getDate(), schedule.getStartTime(),
                10, BigDecimal.ZERO), 11L);
        Booking booking = store(new Booking(tourist, freeDate, 1, "UM-FREE0001", LocalDateTime.now().plusMinutes(10)));

        BookingDto result = bookingService.saveParticipants(TOURIST_USER, 20L, List.of(participant("Ana")));

        assertEquals(BookingStatus.CONFIRMED, result.status());
        verify(emailService).send(eq(tourist.getUser().getEmail()), anyString(), contains(booking.getCode()));
    }

    @Test
    void afterTheTenMinutesTheBookingCannotBeCompleted() {
        store(new Booking(tourist, schedule, 1, "UM-LATE0001", LocalDateTime.now().minusMinutes(1)));

        assertThrows(BusinessRuleException.class,
                () -> bookingService.saveParticipants(TOURIST_USER, 20L, List.of(participant("Ana"))));
    }

    // ---- Cancelling (CU-19, RN-26) ----

    @Test
    void cancellingInsideTheDeadlineReleasesThePlacesAndRefundsThePercentage() {
        Booking booking = confirmedBooking(2);

        BookingDto before = bookingService.getOwn(TOURIST_USER, 20L);
        assertTrue(before.canCancel());
        assertEquals(new BigDecimal("1500.00"), before.refundAmount());   // 50 % of 3000

        BookingDto result = bookingService.cancelOwn(TOURIST_USER, 20L);

        assertEquals(BookingStatus.CANCELLED, result.status());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(10, schedule.getAvailableCapacity());
        verify(refundService).refund(booking, 50);
    }

    @Test
    void cannotCancelAfterTheDeadline() {
        // It can be cancelled until 5 days before, and the date is in 3 days.
        activity.setCancellationPolicy(true, 5 * 24 * 60, true, 50);
        Booking booking = confirmedBooking(2);

        assertThrows(BusinessRuleException.class, () -> bookingService.cancelOwn(TOURIST_USER, 20L));
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(8, schedule.getAvailableCapacity());
    }

    @Test
    void cannotCancelANonCancellableActivity() {
        activity.setCancellationPolicy(false, null, false, null);
        confirmedBooking(1);

        assertThrows(BusinessRuleException.class, () -> bookingService.cancelOwn(TOURIST_USER, 20L));
    }

    @Test
    void cancellableActivityWithoutRefundGivesNothingBack() {
        activity.setCancellationPolicy(true, 60, false, null);
        confirmedBooking(1);

        BookingDto booking = bookingService.getOwn(TOURIST_USER, 20L);

        assertTrue(booking.canCancel());
        assertEquals(BigDecimal.ZERO, booking.refundAmount());

        bookingService.cancelOwn(TOURIST_USER, 20L);
        verify(refundService, never()).refund(any(Booking.class), anyInt());
    }

    @Test
    void aTouristCannotSeeOrCancelAnotherTouristsBooking() {
        confirmedBooking(1);

        assertThrows(ForbiddenException.class, () -> bookingService.getOwn(99L, 20L));
        assertThrows(ForbiddenException.class, () -> bookingService.cancelOwn(99L, 20L));
    }

    // ---- The provider cancels the date (RN-27) ----

    @Test
    void cancellingTheDateCancelsItsBookingsAndTellsTheTourists() {
        Booking booking = confirmedBooking(2);
        when(bookingRepository.findByScheduleIdAndStatusIn(eq(10L), any())).thenReturn(List.of(booking));

        bookingService.cancelAllOf(schedule, "el prestador canceló la fecha");

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(emailService).send(eq(tourist.getUser().getEmail()), anyString(), contains("el prestador canceló la fecha"));
        // The provider cancelled, so the tourist gets back 100 % (RN-27).
        verify(refundService).refund(booking, 100);
        verify(emailService).send(any(), anyString(), contains("100 %"));
    }

    // ---- Automatic expiry (CU-17) ----

    @Test
    void overdueBookingsExpireAndReleaseTheirPlaces() {
        schedule.takePlaces(3);
        Booking overdue = new Booking(tourist, schedule, 3, "UM-OLD00001", LocalDateTime.now().minusMinutes(1));
        when(bookingRepository.findByStatusAndPaymentDeadlineBefore(eq(BookingStatus.PENDING_PAYMENT), any()))
                .thenReturn(List.of(overdue));

        assertEquals(1, bookingService.expireOverdue());

        assertEquals(BookingStatus.EXPIRED, overdue.getStatus());
        assertEquals(10, schedule.getAvailableCapacity());
    }

    // ---- Helpers ----

    /** A booking of the test tourist stored with id 20, with its places already taken. */
    private Booking pendingBooking(int places) {
        schedule.takePlaces(places);
        return store(new Booking(tourist, schedule, places, "UM-TEST0001", LocalDateTime.now().plusMinutes(10)));
    }

    private Booking confirmedBooking(int places) {
        Booking booking = pendingBooking(places);
        booking.confirm();
        return booking;
    }

    private Booking store(Booking booking) {
        withId(booking, 20L);
        when(bookingRepository.findById(20L)).thenReturn(Optional.of(booking));
        return booking;
    }

    private static ParticipantDto participant(String firstName) {
        return new ParticipantDto(firstName, "Pérez", LocalDate.of(1990, 5, 20), "41234567");
    }
}
