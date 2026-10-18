package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uruguaymnatural.backend.dto.BusinessDetailDto;
import com.uruguaymnatural.backend.dto.BusinessRegistrationDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.InvalidDataException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.model.PersonType;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.BusinessRepository;
import com.uruguaymnatural.backend.repository.UserRepository;

class BusinessServiceTest {

    private BusinessRepository businessRepository;
    private UserRepository userRepository;
    private EmailService emailService;
    private BusinessService businessService;

    @BeforeEach
    void setUp() {
        businessRepository = mock(BusinessRepository.class);
        userRepository = mock(UserRepository.class);
        emailService = mock(EmailService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenAnswer(call -> "hash-" + call.getArgument(0));
        businessService = new BusinessService(businessRepository, userRepository, passwordEncoder, emailService);
    }

    // ---- Registration (CU-01) ----

    @Test
    void registersIndividualAsPendingWithDisabledAccount() {
        BusinessDetailDto result = businessService.register(individual("1.234.567-8", "Ana@Mail.com"));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("ana@mail.com", savedUser.getValue().getEmail());
        assertEquals("hash-secreta123", savedUser.getValue().getPassword());
        assertEquals(Role.PROVIDER, savedUser.getValue().getRole());
        assertFalse(savedUser.getValue().isEnabled());

        verify(businessRepository).save(any(Business.class));
        assertEquals(BusinessStatus.PENDING, result.status());
        assertEquals("12345678", result.document());
        assertNull(result.representativeName());
    }

    @Test
    void rejectsRepeatedEmail() {
        when(userRepository.existsByEmailIgnoreCase("ana@mail.com")).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> businessService.register(individual("12345678", "ana@mail.com")));
        verify(businessRepository, never()).save(any());
    }

    @Test
    void individualCanHaveOnlyOneBusiness() {
        when(businessRepository.existsByPersonTypeAndDocument(PersonType.INDIVIDUAL, "12345678"))
                .thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> businessService.register(individual("12345678", "otra@mail.com")));
    }

    @Test
    void rejectsCiWithWrongLength() {
        InvalidDataException e = assertThrows(InvalidDataException.class,
                () -> businessService.register(individual("123", "ana@mail.com")));
        assertEquals("document", e.getField());
    }

    @Test
    void companyNeedsRepresentative() {
        InvalidDataException e = assertThrows(InvalidDataException.class,
                () -> businessService.register(company("211234560018", " ")));
        assertEquals("representativeName", e.getField());
    }

    @Test
    void registersCompanyWithRut() {
        BusinessDetailDto result = businessService.register(company("21 123456 0018", "Juan Pérez"));

        assertEquals(PersonType.COMPANY, result.personType());
        assertEquals("211234560018", result.document());
        assertEquals("Juan Pérez", result.representativeName());
    }

    @Test
    void rejectsUnknownDepartment() {
        BusinessRegistrationDto data = new BusinessRegistrationDto(PersonType.INDIVIDUAL, "12345678",
                "Cabalgatas del Este", "099123456", "ana@mail.com", null, "Cabalgatas", "Narnia",
                "Minas", null, "secreta123");

        InvalidDataException e = assertThrows(InvalidDataException.class, () -> businessService.register(data));
        assertEquals("department", e.getField());
    }

    // ---- Administrator review (CU-02) ----

    @Test
    void approvingEnablesAccountAndSendsEmail() {
        Business business = pendingBusiness();
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        BusinessDetailDto result = businessService.approve(1L);

        assertEquals(BusinessStatus.APPROVED, result.status());
        assertTrue(business.getUser().isEnabled());
        verify(emailService).send(eq("ana@mail.com"), anyString(), anyString());
    }

    @Test
    void rejectingKeepsAccountDisabledAndSendsEmail() {
        Business business = pendingBusiness();
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        BusinessDetailDto result = businessService.reject(1L);

        assertEquals(BusinessStatus.REJECTED, result.status());
        assertFalse(business.getUser().isEnabled());
        verify(emailService).send(eq("ana@mail.com"), anyString(), anyString());
    }

    @Test
    void cannotReviewTwice() {
        Business business = pendingBusiness();
        business.approve();
        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        assertThrows(BusinessRuleException.class, () -> businessService.reject(1L));
        verify(emailService, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void unknownBusinessIsNotFound() {
        when(businessRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> businessService.approve(99L));
    }

    // ---- Helpers ----

    private static BusinessRegistrationDto individual(String ci, String email) {
        return new BusinessRegistrationDto(PersonType.INDIVIDUAL, ci, "Cabalgatas del Este", "099123456",
                email, "no se guarda", "Cabalgatas por las sierras", "Lavalleja", "Minas", null, "secreta123");
    }

    private static BusinessRegistrationDto company(String rut, String representative) {
        return new BusinessRegistrationDto(PersonType.COMPANY, rut, "Aventura Sur S.A.", "42001122",
                "info@aventurasur.uy", representative, "Kayak y trekking", "Rocha", "La Paloma", null,
                "secreta123");
    }

    private static Business pendingBusiness() {
        User user = new User("ana@mail.com", "hash", Role.PROVIDER, false);
        return new Business(user, PersonType.INDIVIDUAL, "12345678", "Cabalgatas del Este", "099123456",
                "ana@mail.com", null, "Cabalgatas", "Lavalleja", "Minas", null);
    }
}
