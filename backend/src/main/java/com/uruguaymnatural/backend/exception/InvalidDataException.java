package com.uruguaymnatural.backend.exception;

/**
 * A form field has an invalid value that simple annotations cannot check
 * (for example, a CI or RUT with the wrong length). Answered with HTTP 400.
 */
public class InvalidDataException extends RuntimeException {

    private final String field;

    public InvalidDataException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
