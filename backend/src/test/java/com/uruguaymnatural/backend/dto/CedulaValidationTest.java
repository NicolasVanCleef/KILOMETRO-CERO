package com.uruguaymnatural.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * The cédula of a tourist and of each participant: 7 or 8 digits, only numbers.
 */
class CedulaValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void acceptsSevenOrEightDigits() {
        assertTrue(participantErrors("1234567").isEmpty());
        assertTrue(participantErrors("41234567").isEmpty());
        assertTrue(touristErrors("41234567").isEmpty());
    }

    @Test
    void rejectsLettersDotsAndDashes() {
        assertOnlyDocumentError(participantErrors("4123456a"));
        assertOnlyDocumentError(participantErrors("4.123.456-7"));
        assertOnlyDocumentError(touristErrors("AB123456"));
    }

    @Test
    void rejectsWrongLength() {
        assertOnlyDocumentError(participantErrors("123456"));
        assertOnlyDocumentError(participantErrors("123456789"));
        assertOnlyDocumentError(touristErrors("123"));
    }

    private static <T> void assertOnlyDocumentError(Set<ConstraintViolation<T>> errors) {
        assertEquals(1, errors.size());
        ConstraintViolation<T> error = errors.iterator().next();
        assertEquals("document", error.getPropertyPath().toString());
        assertEquals("La cédula tiene que tener 7 u 8 dígitos, solo números.", error.getMessage());
    }

    private static Set<ConstraintViolation<ParticipantDto>> participantErrors(String document) {
        return validator.validate(new ParticipantDto("Ana", "Pérez", LocalDate.of(1990, 5, 20), document));
    }

    private static Set<ConstraintViolation<TouristRegistrationDto>> touristErrors(String document) {
        return validator.validate(new TouristRegistrationDto("Sofía", "Martínez", "Uruguay",
                LocalDate.of(1995, 3, 10), "Español", document, "sofia@mail.com", "secret123"));
    }
}
