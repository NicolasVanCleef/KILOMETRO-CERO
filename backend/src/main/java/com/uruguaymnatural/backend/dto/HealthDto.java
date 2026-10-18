package com.uruguaymnatural.backend.dto;

/**
 * Response of GET /api/health.
 *
 * @param status   "UP" when the backend is running
 * @param database "UP" or "DOWN" depending on the database connection
 */
public record HealthDto(String status, String database) {
}
