package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.uruguaymnatural.backend.dto.LoginDto;
import com.uruguaymnatural.backend.dto.SessionDto;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.UnauthorizedException;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.PersonType;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.BusinessRepository;
import com.uruguaymnatural.backend.repository.TouristRepository;
import com.uruguaymnatural.backend.repository.UserRepository;
import com.uruguaymnatural.backend.security.JwtService;

class AuthServiceTest {

    private UserRepository userRepository;
    private BusinessRepository businessRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        businessRepository = mock(BusinessRepository.class);
        TouristRepository touristRepository = mock(TouristRepository.class);
        // Fake encoder: the "hash" of a password is "hash-" + password.
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.matches(any(), any()))
                .thenAnswer(call -> ("hash-" + call.getArgument(0)).equals(call.getArgument(1)));
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.createToken(any())).thenReturn("the-token");
        authService = new AuthService(userRepository, touristRepository, businessRepository,
                passwordEncoder, jwtService);
    }

    @Test
    void loginWithRightPasswordReturnsTokenAndUser() {
        save(new User("admin@mail.com", "hash-secret123", Role.ADMIN, true));

        SessionDto session = authService.login(new LoginDto("admin@mail.com", "secret123"));

        assertEquals("the-token", session.token());
        assertEquals(Role.ADMIN, session.user().role());
        assertEquals("Administrador", session.user().displayName());
    }

    @Test
    void wrongPasswordIsRejected() {
        save(new User("admin@mail.com", "hash-secret123", Role.ADMIN, true));

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginDto("admin@mail.com", "other")));
    }

    @Test
    void unknownEmailIsRejectedWithTheSameError() {
        when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> authService.login(new LoginDto("nobody@mail.com", "secret123")));
    }

    @Test
    void providerWithPendingBusinessCannotLogIn() {
        User user = save(new User("ana@mail.com", "hash-secret123", Role.PROVIDER, false));
        when(businessRepository.findByUserId(any())).thenReturn(Optional.of(business(user)));

        ForbiddenException e = assertThrows(ForbiddenException.class,
                () -> authService.login(new LoginDto("ana@mail.com", "secret123")));
        assertTrue(e.getMessage().contains("todavía no fue aprobado"));
    }

    @Test
    void providerWithRejectedBusinessCannotLogIn() {
        User user = save(new User("ana@mail.com", "hash-secret123", Role.PROVIDER, false));
        Business business = business(user);
        business.reject();
        when(businessRepository.findByUserId(any())).thenReturn(Optional.of(business));

        ForbiddenException e = assertThrows(ForbiddenException.class,
                () -> authService.login(new LoginDto("ana@mail.com", "secret123")));
        assertTrue(e.getMessage().contains("no fue aprobado"));
    }

    @Test
    void providerWithApprovedBusinessLogsInWithBusinessName() {
        User user = save(new User("ana@mail.com", "hash-secret123", Role.PROVIDER, false));
        Business business = business(user);
        business.approve();
        when(businessRepository.findByUserId(any())).thenReturn(Optional.of(business));

        SessionDto session = authService.login(new LoginDto("ana@mail.com", "secret123"));

        assertEquals("Cabalgatas Ana", session.user().displayName());
    }

    private User save(User user) {
        ReflectionTestUtils.setField(user, "id", 7L);
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        return user;
    }

    private static Business business(User user) {
        return new Business(user, PersonType.INDIVIDUAL, "12345678", "Cabalgatas Ana", "099123456",
                user.getEmail(), null, "Cabalgatas por la sierra", "Rocha", "La Paloma", null);
    }
}
