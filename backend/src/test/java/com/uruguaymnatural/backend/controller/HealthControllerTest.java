package com.uruguaymnatural.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.dto.HealthDto;
import com.uruguaymnatural.backend.service.HealthService;

class HealthControllerTest {

    private final HealthService healthService = mock(HealthService.class);
    private final HealthController healthController = new HealthController(healthService);

    @Test
    void reportsDatabaseUpWhenConnectionWorks() {
        when(healthService.isDatabaseUp()).thenReturn(true);

        HealthDto health = healthController.getHealth();

        assertEquals("UP", health.status());
        assertEquals("UP", health.database());
    }

    @Test
    void reportsDatabaseDownWhenConnectionFails() {
        when(healthService.isDatabaseUp()).thenReturn(false);

        HealthDto health = healthController.getHealth();

        assertEquals("UP", health.status());
        assertEquals("DOWN", health.database());
    }
}
