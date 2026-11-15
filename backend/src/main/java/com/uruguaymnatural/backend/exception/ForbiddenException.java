package com.uruguaymnatural.backend.exception;

/**
 * The user is logged in but their role cannot do this (RNF-16). Answered with HTTP 403.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
