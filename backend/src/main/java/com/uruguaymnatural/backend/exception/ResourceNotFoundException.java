package com.uruguaymnatural.backend.exception;

/**
 * The requested resource does not exist. Answered with HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
