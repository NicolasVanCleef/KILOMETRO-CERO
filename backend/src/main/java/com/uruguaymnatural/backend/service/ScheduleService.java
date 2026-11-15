package com.uruguaymnatural.backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.ScheduleDto;
import com.uruguaymnatural.backend.dto.ScheduleRequestDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.ScheduleStatus;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.ActivityScheduleRepository;

/**
 * The provider schedules an activity on concrete dates (CU-11) and cancels dates (CU-12).
 */
@Service
public class ScheduleService {

    private final ActivityScheduleRepository scheduleRepository;
    private final ActivityRepository activityRepository;

    public ScheduleService(ActivityScheduleRepository scheduleRepository, ActivityRepository activityRepository) {
        this.scheduleRepository = scheduleRepository;
        this.activityRepository = activityRepository;
    }

    @Transactional(readOnly = true)
    public List<ScheduleDto> listOwn(Long userId, Long activityId) {
        findOwnActivity(userId, activityId);
        return scheduleRepository.findByActivityIdOrderByDateAscStartTimeAsc(activityId).stream()
                .map(ScheduleDto::from)
                .toList();
    }

    /**
     * Adds a date. Capacity and price default to the activity's, and each date can have its
     * own (RN-08).
     */
    @Transactional
    public ScheduleDto create(Long userId, Long activityId, ScheduleRequestDto data) {
        Activity activity = findOwnActivity(userId, activityId);

        if (!LocalDateTime.of(data.date(), data.startTime()).isAfter(LocalDateTime.now())) {
            throw new InvalidDataException("date", "La fecha y hora ya pasaron.");
        }
        if (scheduleRepository.existsByActivityIdAndDateAndStartTimeAndStatus(activityId, data.date(),
                data.startTime(), ScheduleStatus.SCHEDULED)) {
            throw new BusinessRuleException("La actividad ya está programada ese día a esa hora.");
        }

        int capacity = data.capacity() != null ? data.capacity() : activity.getDefaultCapacity();
        BigDecimal price = data.price() != null ? data.price() : activity.getDefaultPrice();
        ActivitySchedule schedule = new ActivitySchedule(activity, data.date(), data.startTime(), capacity, price);
        return ScheduleDto.from(scheduleRepository.save(schedule));
    }

    /** The provider cancels one date, for example because of rain (CU-12). */
    @Transactional
    public ScheduleDto cancel(Long userId, Long scheduleId) {
        ActivitySchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la fecha " + scheduleId + "."));
        checkOwner(userId, schedule.getActivity());
        if (!schedule.isScheduled()) {
            throw new BusinessRuleException("Esta fecha ya está cancelada.");
        }
        if (!schedule.getStart().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Esta fecha ya pasó, no se puede cancelar.");
        }
        cancel(schedule, "el prestador canceló la fecha");
        return ScheduleDto.from(schedule);
    }

    /** Cancels every date of an activity that has not happened yet (the activity was deleted). */
    @Transactional
    public void cancelAllOf(Activity activity, String reason) {
        LocalDateTime now = LocalDateTime.now();
        scheduleRepository.findByActivityIdAndStatus(activity.getId(), ScheduleStatus.SCHEDULED).stream()
                .filter(schedule -> schedule.getStart().isAfter(now))
                .forEach(schedule -> cancel(schedule, reason));
    }

    private void cancel(ActivitySchedule schedule, String reason) {
        schedule.cancel();
    }

    private Activity findOwnActivity(Long userId, Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .filter(Activity::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la actividad " + activityId + "."));
        checkOwner(userId, activity);
        return activity;
    }

    private static void checkOwner(Long userId, Activity activity) {
        if (!activity.getBusiness().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Esta actividad es de otro emprendimiento.");
        }
    }
}
