package com.uruguaymnatural.backend.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.uruguaymnatural.backend.model.PersonType;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class BusinessRegistrationDtoTest {

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
    void acceptsPhoneWithNineDigits() {
        assertTrue(phoneErrors("099123456").isEmpty());
    }

    @Test
    void rejectsPhoneWithLetters() {
        assertEquals(1, phoneErrors("09912345a").size());
    }

    @Test
    void rejectsPhoneWithWrongLength() {
        assertEquals(1, phoneErrors("42001122").size());
        assertEquals(1, phoneErrors("0991234567").size());
    }

    @Test
    void rejectsPhoneWithSpaces() {
        assertEquals(1, phoneErrors("099 123 456").size());
    }

    private static Set<ConstraintViolation<BusinessRegistrationDto>> phoneErrors(String phone) {
        BusinessRegistrationDto data = new BusinessRegistrationDto(PersonType.INDIVIDUAL, "12345678",
                "Cabalgatas del Este", phone, "ana@mail.com", null, "Cabalgatas por las sierras",
                "Lavalleja", "Minas", null, "secreta123");
        return validator.validateProperty(data, "phone");
    }
}
