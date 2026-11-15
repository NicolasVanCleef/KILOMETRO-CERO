package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uruguaymnatural.backend.dto.TouristProfileDto;
import com.uruguaymnatural.backend.dto.TouristProfileUpdateDto;
import com.uruguaymnatural.backend.dto.TouristRegistrationDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.TouristRepository;
import com.uruguaymnatural.backend.repository.UserRepository;

class TouristServiceTest {

    private TouristRepository touristRepository;
    private UserRepository userRepository;
    private TouristService touristService;

    @BeforeEach
    void setUp() {
        touristRepository = mock(TouristRepository.class);
        userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenAnswer(call -> "hash-" + call.getArgument(0));
        touristService = new TouristService(touristRepository, userRepository, passwordEncoder);
    }

    @Test
    void registersTouristWithEnabledAccountAndHashedPassword() {
        TouristProfileDto result = touristService.register(registration("Sofia@Mail.com"));

        ArgumentCaptor<User> savedUser = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("sofia@mail.com", savedUser.getValue().getEmail());
        assertEquals("hash-secret123", savedUser.getValue().getPassword());
        assertEquals(Role.TOURIST, savedUser.getValue().getRole());
        assertTrue(savedUser.getValue().isEnabled());

        assertEquals("Sofía", result.firstName());
        assertEquals("Brasil", result.country());
        verify(touristRepository).save(any(Tourist.class));
    }

    @Test
    void repeatedEmailIsRejected() {
        when(userRepository.existsByEmailIgnoreCase("sofia@mail.com")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> touristService.register(registration("sofia@mail.com")));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatesProfileData() {
        User user = new User("sofia@mail.com", "hash", Role.TOURIST, true);
        Tourist tourist = new Tourist(user, "Sofía", "Martínez", "Brasil", LocalDate.of(1995, 3, 10),
                "Portugués", "AB123456");
        when(touristRepository.findByUserId(4L)).thenReturn(Optional.of(tourist));

        TouristProfileDto result = touristService.updateProfile(4L, new TouristProfileUpdateDto(
                "Sofía", "Martínez", "Uruguay", LocalDate.of(1995, 3, 10), "Español", "4.123.456-7"));

        assertEquals("Uruguay", result.country());
        assertEquals("Español", tourist.getPreferredLanguage());
        assertEquals("sofia@mail.com", result.email());
    }

    private static TouristRegistrationDto registration(String email) {
        return new TouristRegistrationDto("Sofía", "Martínez", "Brasil", LocalDate.of(1995, 3, 10),
                "Portugués", "AB123456", email, "secret123");
    }
}
