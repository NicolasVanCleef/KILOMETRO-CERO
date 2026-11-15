package com.uruguaymnatural.backend.service;

import static com.uruguaymnatural.backend.TestData.withId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.dto.ActivityDto;
import com.uruguaymnatural.backend.dto.ActivityRequestDto;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.Equipment;
import com.uruguaymnatural.backend.model.Tag;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.EquipmentRepository;
import com.uruguaymnatural.backend.repository.TagRepository;

class ActivityServiceTest {

    private static final Long PROVIDER = 5L;

    private ActivityRepository activityRepository;
    private ScheduleService scheduleService;
    private ActivityService activityService;
    private final Business business = TestData.business(PROVIDER);

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        when(activityRepository.save(any())).thenAnswer(call -> call.getArgument(0));
        TagRepository tagRepository = mock(TagRepository.class);
        when(tagRepository.findAllById(List.of(1L))).thenReturn(List.of(withId(new Tag("Trekking"), 1L)));
        EquipmentRepository equipmentRepository = mock(EquipmentRepository.class);
        when(equipmentRepository.findAllById(List.of())).thenReturn(List.of());
        when(equipmentRepository.findAllById(List.of(1L))).thenReturn(List.of(withId(new Equipment("Casco"), 1L)));
        when(equipmentRepository.findAllById(List.of(2L))).thenReturn(List.of(withId(new Equipment("Agua"), 2L)));
        BusinessService businessService = mock(BusinessService.class);
        when(businessService.findByUser(PROVIDER)).thenReturn(business);
        scheduleService = mock(ScheduleService.class);
        activityService = new ActivityService(activityRepository, tagRepository, equipmentRepository,
                businessService, scheduleService);
    }

    @Test
    void createsActivityWithTagsAndEquipment() {
        ActivityDto result = activityService.create(PROVIDER, request(List.of(1L), true, 120, true, 50));

        assertEquals("Trekking al cerro", result.name());
        assertEquals("Cabalgatas Ana", result.businessName());
        assertEquals("Trekking", result.tags().get(0).name());
        assertEquals("Agua", result.requiredEquipment().get(0).name());
        assertEquals("Casco", result.providedEquipment().get(0).name());
        assertEquals(50, result.refundPercentage());
    }

    @Test
    void activityWithoutTagIsRejected() {
        InvalidDataException e = assertThrows(InvalidDataException.class,
                () -> activityService.create(PROVIDER, request(List.of(99L), false, null, false, null)));
        assertEquals("tagIds", e.getField());
    }

    @Test
    void refundPercentageOutOfRangeIsRejected() {
        InvalidDataException e = assertThrows(InvalidDataException.class,
                () -> activityService.create(PROVIDER, request(List.of(1L), true, 120, true, 150)));
        assertEquals("refundPercentage", e.getField());
    }

    @Test
    void cancellableActivityNeedsADeadline() {
        InvalidDataException e = assertThrows(InvalidDataException.class,
                () -> activityService.create(PROVIDER, request(List.of(1L), true, null, false, null)));
        assertEquals("cancellationMinutes", e.getField());
    }

    @Test
    void nonCancellableActivityKeepsNoRefundData() {
        ActivityDto result = activityService.create(PROVIDER, request(List.of(1L), false, 120, true, 50));

        assertFalse(result.cancellable());
        assertFalse(result.refundable());
        assertNull(result.cancellationMinutes());
        assertNull(result.refundPercentage());
    }

    @Test
    void providerCannotTouchAnotherBusinessActivity() {
        Activity other = TestData.activity(3L, TestData.business(8L));
        when(activityRepository.findById(3L)).thenReturn(Optional.of(other));

        assertThrows(ForbiddenException.class,
                () -> activityService.update(PROVIDER, 3L, request(List.of(1L), false, null, false, null)));
        assertThrows(ForbiddenException.class, () -> activityService.delete(PROVIDER, 3L));
    }

    @Test
    void deletingAnActivityUnpublishesItAndCancelsItsDates() {
        Activity activity = TestData.activity(3L, business);
        when(activityRepository.findById(3L)).thenReturn(Optional.of(activity));

        activityService.delete(PROVIDER, 3L);

        assertFalse(activity.isActive());
        verify(scheduleService).cancelAllOf(eq(activity), anyString());
    }

    private static ActivityRequestDto request(List<Long> tagIds, boolean cancellable, Integer cancellationMinutes,
            boolean refundable, Integer refundPercentage) {
        return new ActivityRequestDto("Trekking al cerro", "Subida guiada al cerro", "Mayores de 12 años", null,
                "Lavalleja", "Minas", null, null, null, 180, 12, new BigDecimal("900"), 60, null,
                cancellable, cancellationMinutes, refundable, refundPercentage, tagIds, List.of(2L), List.of(1L));
    }
}
