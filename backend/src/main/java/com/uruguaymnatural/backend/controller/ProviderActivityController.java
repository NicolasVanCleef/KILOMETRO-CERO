package com.uruguaymnatural.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.ActivityDto;
import com.uruguaymnatural.backend.dto.ActivityRequestDto;
import com.uruguaymnatural.backend.dto.ScheduleDto;
import com.uruguaymnatural.backend.dto.ScheduleRequestDto;
import com.uruguaymnatural.backend.security.AuthUser;
import com.uruguaymnatural.backend.service.ActivityService;
import com.uruguaymnatural.backend.service.ScheduleService;

import jakarta.validation.Valid;

/**
 * The provider's activities and their dates (CU-10, CU-11, CU-12). A provider only reaches
 * the activities of their own business.
 */
@RestController
@RequestMapping("/api/provider")
public class ProviderActivityController {

    private final ActivityService activityService;
    private final ScheduleService scheduleService;

    public ProviderActivityController(ActivityService activityService, ScheduleService scheduleService) {
        this.activityService = activityService;
        this.scheduleService = scheduleService;
    }

    /** GET /api/provider/activities — the activities of the logged-in provider. */
    @GetMapping("/activities")
    public List<ActivityDto> list(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user) {
        return activityService.listOwn(user.id());
    }

    /** POST /api/provider/activities — creates an activity. 201. */
    @PostMapping("/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityDto create(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user,
            @Valid @RequestBody ActivityRequestDto data) {
        return activityService.create(user.id(), data);
    }

    /** GET /api/provider/activities/{id} — one activity, to fill the edit form. */
    @GetMapping("/activities/{id}")
    public ActivityDto get(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id) {
        return activityService.getOwn(user.id(), id);
    }

    /** PUT /api/provider/activities/{id} — modifies an activity. */
    @PutMapping("/activities/{id}")
    public ActivityDto update(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id,
            @Valid @RequestBody ActivityRequestDto data) {
        return activityService.update(user.id(), id, data);
    }

    /** DELETE /api/provider/activities/{id} — deletes an activity and cancels its dates. 204. */
    @DeleteMapping("/activities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id) {
        activityService.delete(user.id(), id);
    }

    /** GET /api/provider/activities/{id}/schedules — all the dates of an activity. */
    @GetMapping("/activities/{id}/schedules")
    public List<ScheduleDto> listSchedules(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user,
            @PathVariable Long id) {
        return scheduleService.listOwn(user.id(), id);
    }

    /** POST /api/provider/activities/{id}/schedules — adds a date. 201. */
    @PostMapping("/activities/{id}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleDto createSchedule(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id,
            @Valid @RequestBody ScheduleRequestDto data) {
        return scheduleService.create(user.id(), id, data);
    }

    /** POST /api/provider/schedules/{id}/cancel — cancels a date. */
    @PostMapping("/schedules/{id}/cancel")
    public ScheduleDto cancelSchedule(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id) {
        return scheduleService.cancel(user.id(), id);
    }
}
