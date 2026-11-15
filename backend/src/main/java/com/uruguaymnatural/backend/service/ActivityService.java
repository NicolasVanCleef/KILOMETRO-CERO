package com.uruguaymnatural.backend.service;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.ActivityDto;
import com.uruguaymnatural.backend.dto.ActivityRequestDto;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Activity;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.Departments;
import com.uruguaymnatural.backend.model.Equipment;
import com.uruguaymnatural.backend.model.Tag;
import com.uruguaymnatural.backend.repository.ActivityRepository;
import com.uruguaymnatural.backend.repository.EquipmentRepository;
import com.uruguaymnatural.backend.repository.TagRepository;

/**
 * The provider manages the activities of their business (CU-10, RF-34).
 */
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final TagRepository tagRepository;
    private final EquipmentRepository equipmentRepository;
    private final BusinessService businessService;
    private final ScheduleService scheduleService;

    public ActivityService(ActivityRepository activityRepository, TagRepository tagRepository,
            EquipmentRepository equipmentRepository, BusinessService businessService,
            ScheduleService scheduleService) {
        this.activityRepository = activityRepository;
        this.tagRepository = tagRepository;
        this.equipmentRepository = equipmentRepository;
        this.businessService = businessService;
        this.scheduleService = scheduleService;
    }

    @Transactional(readOnly = true)
    public List<ActivityDto> listOwn(Long userId) {
        Business business = businessService.findByUser(userId);
        return activityRepository.findByBusinessIdAndActiveTrueOrderByNameAsc(business.getId()).stream()
                .map(ActivityDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActivityDto getOwn(Long userId, Long activityId) {
        return ActivityDto.from(findOwn(userId, activityId));
    }

    @Transactional
    public ActivityDto create(Long userId, ActivityRequestDto data) {
        Activity activity = new Activity(businessService.findByUser(userId));
        apply(activity, data);
        return ActivityDto.from(activityRepository.save(activity));
    }

    @Transactional
    public ActivityDto update(Long userId, Long activityId, ActivityRequestDto data) {
        Activity activity = findOwn(userId, activityId);
        apply(activity, data);
        return ActivityDto.from(activity);
    }

    /**
     * The provider deletes an activity (CU-10, 1b). It is not erased: it stops being published
     * and all its dates are cancelled, which refunds 100 % to the tourists who paid (RN-27).
     */
    @Transactional
    public void delete(Long userId, Long activityId) {
        remove(findOwn(userId, activityId), "el prestador eliminó la actividad");
    }

    /** Removes an activity from the portal. Also used when the administrator deletes it (RN-28). */
    @Transactional
    public void remove(Activity activity, String reason) {
        scheduleService.cancelAllOf(activity, reason);
        activity.deactivate();
    }

    /**
     * The activity, only if it belongs to the logged-in provider (RNF-16).
     */
    public Activity findOwn(Long userId, Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .filter(Activity::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la actividad " + activityId + "."));
        if (!activity.getBusiness().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Esta actividad es de otro emprendimiento.");
        }
        return activity;
    }

    /** Checks the rules that annotations cannot check and copies the form data to the activity. */
    private void apply(Activity activity, ActivityRequestDto data) {
        if (!Departments.isValid(data.department())) {
            throw new InvalidDataException("department", "Elegí un departamento de la lista.");
        }
        if (data.maxBookingMinutes() != null && data.maxBookingMinutes() <= data.minBookingMinutes()) {
            throw new InvalidDataException("maxBookingMinutes",
                    "Tiene que ser mayor que los minutos mínimos antes del inicio.");
        }

        // Cancellation policy (RN-25): the deadline only makes sense if it can be cancelled,
        // and the percentage only if it has a refund.
        Integer cancellationMinutes = null;
        boolean refundable = false;
        Integer refundPercentage = null;
        if (data.cancellable()) {
            if (data.cancellationMinutes() == null) {
                throw new InvalidDataException("cancellationMinutes",
                        "Indicá hasta cuántos minutos antes del inicio se puede cancelar.");
            }
            cancellationMinutes = data.cancellationMinutes();
            refundable = data.refundable();
            if (refundable) {
                if (data.refundPercentage() == null || data.refundPercentage() < 0 || data.refundPercentage() > 100) {
                    throw new InvalidDataException("refundPercentage", "El porcentaje tiene que estar entre 0 y 100.");
                }
                refundPercentage = data.refundPercentage();
            }
        }

        // RN-05: at least one tag. RN-06: only from the administrator's lists.
        Set<Tag> tags = new LinkedHashSet<>(tagRepository.findAllById(data.tagIds()));
        if (tags.isEmpty() || tags.size() != new HashSet<>(data.tagIds()).size()) {
            throw new InvalidDataException("tagIds", "Elegí al menos una etiqueta de la lista.");
        }
        List<Long> requiredIds = data.requiredEquipmentIds() == null ? List.of() : data.requiredEquipmentIds();
        List<Long> providedIds = data.providedEquipmentIds() == null ? List.of() : data.providedEquipmentIds();
        if (requiredIds.stream().anyMatch(providedIds::contains)) {
            throw new InvalidDataException("providedEquipmentIds",
                    "Un mismo equipamiento no puede ser requerido y provisto a la vez.");
        }
        List<Equipment> required = findEquipment(requiredIds, "requiredEquipmentIds");
        List<Equipment> provided = findEquipment(providedIds, "providedEquipmentIds");

        activity.setDescriptionData(data.name().trim(), data.description().trim(),
                blankToNull(data.requirements()), blankToNull(data.observations()), data.durationMinutes());
        activity.setLocation(data.department(), data.locality().trim(), blankToNull(data.address()),
                data.latitude(), data.longitude());
        activity.setBookingRules(data.defaultCapacity(), data.defaultPrice(), data.minBookingMinutes(),
                data.maxBookingMinutes());
        activity.setCancellationPolicy(data.cancellable(), cancellationMinutes, refundable, refundPercentage);
        activity.setTags(tags);
        activity.setEquipment(required, provided);
    }

    private List<Equipment> findEquipment(List<Long> ids, String field) {
        List<Equipment> found = equipmentRepository.findAllById(ids);
        if (found.size() != new HashSet<>(ids).size()) {
            throw new InvalidDataException(field, "Elegí el equipamiento de la lista.");
        }
        return found;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
