package com.uruguaymnatural.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.ActivityDto;
import com.uruguaymnatural.backend.service.AdminModerationService;

/**
 * The administrator removes providers (CU-23) and inappropriate activities (CU-24).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminModerationController {

    private final AdminModerationService moderationService;

    public AdminModerationController(AdminModerationService moderationService) {
        this.moderationService = moderationService;
    }

    /** DELETE /api/admin/businesses/{id} — removes an approved provider. 204. */
    @DeleteMapping("/businesses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeBusiness(@PathVariable Long id) {
        moderationService.removeBusiness(id);
    }

    /** GET /api/admin/activities — every published activity. */
    @GetMapping("/activities")
    public List<ActivityDto> listActivities() {
        return moderationService.listActivities();
    }

    /** DELETE /api/admin/activities/{id} — removes an inappropriate activity. 204. */
    @DeleteMapping("/activities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeActivity(@PathVariable Long id) {
        moderationService.removeActivity(id);
    }
}
