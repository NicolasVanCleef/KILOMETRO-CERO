package com.uruguaymnatural.backend.exception;

import java.util.Map;

/**
 * Error body returned by the API.
 *
 * @param message     general message to show to the user
 * @param fieldErrors message per form field, when the data is invalid (may be empty)
 */
public record ApiError(String message, Map<String, String> fieldErrors) {

    public ApiError(String message) {
        this(message, Map.of());
    }
}
