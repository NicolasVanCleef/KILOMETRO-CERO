package com.uruguaymnatural.backend.exception;

/**
 * The request needs a logged-in user and there is none, or the login data is wrong.
 * Answered with HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
