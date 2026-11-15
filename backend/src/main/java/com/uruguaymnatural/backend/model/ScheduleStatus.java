package com.uruguaymnatural.backend.model;

/**
 * A date of an activity is SCHEDULED until the provider (or the administrator) cancels it.
 * A cancelled date is kept, not deleted, so its bookings and refunds can be traced.
 */
public enum ScheduleStatus {
    SCHEDULED,
    CANCELLED
}
