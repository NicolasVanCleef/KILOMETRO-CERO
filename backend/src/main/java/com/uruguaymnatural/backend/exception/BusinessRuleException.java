package com.uruguaymnatural.backend.exception;

/**
 * The request breaks a business rule (for example, a repeated email). Answered with HTTP 409.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
