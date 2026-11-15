package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.dto.ScheduleDto;
import com.uruguaymnatural.backend.dto.ScheduleRequestDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.ScheduleStatus;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;

class ScheduleServiceTest {

    private static final Long PROVIDER = 5L;
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    private ActivityScheduleRepository scheduleRepository;
    private BookingService bookingService;
    private ScheduleService scheduleService;
    private final Activity activity = TestData.activity(3L, TestData.business(PROVIDER));

    @BeforeEach
    void setUp() {
        scheduleRepository = mock(ActivityScheduleRepository.class);
        when(scheduleRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        ActivityRepository activityRepository = mock(ActivityRepository.class);
        when(activityRepository.findById(3L)).thenReturn(Optional.of(activity));
        bookingService = mock(BookingService.class);
        scheduleService = new ScheduleService(scheduleRepository, activityRepository, bookingService);
    }

    @Test
    void newDateUsesTheActivityDefaults() {
        ScheduleDto result = scheduleService.create(PROVIDER, 3L,
                new ScheduleRequestDto(TOMORROW, LocalTime.of(10, 0), null, null));

        assertEquals(10, result.totalCapacity());
        assertEquals(10, result.availableCapacity());
        assertEquals(new BigDecimal("1500"), result.price());
        assertEquals(ScheduleStatus.SCHEDULED, result.status());
    }

    @Test
    void newDateCanHaveItsOwnCapacityAndPrice() {
        ScheduleDto result = scheduleService.create(PROVIDER, 3L,
                new ScheduleRequestDto(TOMORROW, LocalTime.of(10, 0), 6, new BigDecimal("1200")));

        assertEquals(6, result.totalCapacity());
        assertEquals(new BigDecimal("1200"), result.price());
    }

    @Test
    void pastDateIsRejected() {
        InvalidDataException e = assertThrows(InvalidDataException.class, () -> scheduleService.create(PROVIDER, 3L,
                new ScheduleRequestDto(LocalDate.now().minusDays(1), LocalTime.of(10, 0), null, null)));
        assertEquals("date", e.getField());
        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void sameDayAndTimeTwiceIsRejected() {
        when(scheduleRepository.existsByActivityIdAndDateAndStartTimeAndStatus(3L, TOMORROW, LocalTime.of(10, 0),
                ScheduleStatus.SCHEDULED)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> scheduleService.create(PROVIDER, 3L,
                new ScheduleRequestDto(TOMORROW, LocalTime.of(10, 0), null, null)));
    }

    @Test
    void anotherProviderCannotAddDates() {
        assertThrows(ForbiddenException.class, () -> scheduleService.create(8L, 3L,
                new ScheduleRequestDto(TOMORROW, LocalTime.of(10, 0), null, null)));
    }

    @Test
    void cancellingADateMarksItCancelled() {
        ActivitySchedule schedule = TestData.schedule(7L, activity, LocalDateTime.now().plusDays(2));
        when(scheduleRepository.findById(7L)).thenReturn(Optional.of(schedule));

        assertEquals(ScheduleStatus.CANCELLED, scheduleService.cancel(PROVIDER, 7L).status());
        verify(bookingService).cancelAllOf(eq(schedule), anyString());
        assertThrows(BusinessRuleException.class, () -> scheduleService.cancel(PROVIDER, 7L));
    }
}
