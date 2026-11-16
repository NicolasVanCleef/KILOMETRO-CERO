package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.TestData;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.BusinessRepository;

class AdminModerationServiceTest {

    private BusinessRepository businessRepository;
    private ActivityRepository activityRepository;
    private ActivityService activityService;
    private EmailService emailService;
    private AdminModerationService moderationService;

    private final Business business = TestData.business(5L);   // approved, id 50
    private final Activity activity = TestData.activity(1L, business);

    @BeforeEach
    void setUp() {
        businessRepository = mock(BusinessRepository.class);
        activityRepository = mock(ActivityRepository.class);
        activityService = mock(ActivityService.class);
        emailService = mock(EmailService.class);
        moderationService = new AdminModerationService(businessRepository, activityRepository, activityService,
                emailService);
    }

    @Test
    void removingAnActivityTakesItDownAndTellsTheProvider() {
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        moderationService.removeActivity(1L);

        // ActivityService.remove unpublishes it, cancels its dates and refunds 100 % (RN-28).
        verify(activityService).remove(eq(activity), anyString());
        verify(emailService).send(eq(business.getEmail()), anyString(), contains("Cabalgata por la sierra"));
    }

    @Test
    void anActivityAlreadyDeletedIsNotFound() {
        activity.deactivate();
        when(activityRepository.findById(1L)).thenReturn(Optional.of(activity));

        assertThrows(ResourceNotFoundException.class, () -> moderationService.removeActivity(1L));
        verify(activityService, never()).remove(any(), anyString());
    }

    @Test
    void removingAProviderDisablesTheirLoginAndTakesDownTheirActivities() {
        when(businessRepository.findById(50L)).thenReturn(Optional.of(business));
        when(activityRepository.findByBusinessIdAndActiveTrueOrderByNameAsc(50L)).thenReturn(List.of(activity));

        moderationService.removeBusiness(50L);

        assertEquals(BusinessStatus.REMOVED, business.getStatus());
        assertFalse(business.getUser().isEnabled());
        verify(activityService).remove(eq(activity), anyString());
        verify(emailService).send(eq(business.getEmail()), anyString(), anyString());
    }

    @Test
    void onlyAnApprovedProviderCanBeRemoved() {
        business.reject();
        when(businessRepository.findById(50L)).thenReturn(Optional.of(business));

        assertThrows(BusinessRuleException.class, () -> moderationService.removeBusiness(50L));
    }
}
