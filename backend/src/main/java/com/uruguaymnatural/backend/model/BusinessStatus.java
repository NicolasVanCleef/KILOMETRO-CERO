package com.uruguaymnatural.backend.model;

/**
 * A new business stays PENDING until an administrator approves or rejects it (RN-01).
 * REMOVED = it was approved and later the administrator removed the provider (CU-23).
 */
public enum BusinessStatus {
    PENDING,
    APPROVED,
    REJECTED,
    REMOVED
}
