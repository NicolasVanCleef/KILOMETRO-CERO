package com.uruguaymnatural.backend.model;

/**
 * A new business stays PENDING until an administrator approves or rejects it (RN-01).
 */
public enum BusinessStatus {
    PENDING,
    APPROVED,
    REJECTED
}
