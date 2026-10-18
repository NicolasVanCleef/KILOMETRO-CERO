package com.uruguaymnatural.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.HealthDto;
import com.uruguaymnatural.backend.service.HealthService;

/**
 * Lets the frontend check that the backend and the database are running.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public HealthDto getHealth() {
        String database = healthService.isDatabaseUp() ? "UP" : "DOWN";
        return new HealthDto("UP", database);
    }
}
