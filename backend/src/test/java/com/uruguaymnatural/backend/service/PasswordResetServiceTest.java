package com.uruguaymnatural.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.PasswordResetToken;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.PasswordResetTokenRepository;
import com.uruguaymnatural.backend.repository.UserRepository;

class PasswordResetServiceTest {

    private UserRepository userRepository;
    private PasswordResetTokenRepository tokenRepository;
    private EmailService emailService;
    private PasswordResetService service;
    private final User user = new User("sofia@mail.com", "hash-old", Role.TOURIST, true);

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        tokenRepository = mock(PasswordResetTokenRepository.class);
        emailService = mock(EmailService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenAnswer(call -> "hash-" + call.getArgument(0));
        service = new PasswordResetService(userRepository, tokenRepository, passwordEncoder, emailService,
                "http://localhost:5173", 30);
    }

    @Test
    void sendsEmailWithLinkThatHasTheToken() {
        when(userRepository.findByEmailIgnoreCase("sofia@mail.com")).thenReturn(Optional.of(user));

        service.requestReset("sofia@mail.com");

        ArgumentCaptor<PasswordResetToken> saved = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(saved.capture());
        verify(emailService).send(eq("sofia@mail.com"), anyString(),
                contains("http://localhost:5173/restablecer?token=" + saved.getValue().getToken()));
    }

    @Test
    void unknownEmailSendsNothing() {
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.requestReset("nobody@mail.com"));
        verify(emailService, never()).send(any(), any(), any());
    }

    @Test
    void validLinkChangesThePasswordAndCannotBeUsedAgain() {
        PasswordResetToken token = new PasswordResetToken("abc", user, LocalDateTime.now().plusMinutes(30));
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        service.reset("abc", "newsecret1");

        assertEquals("hash-newsecret1", user.getPassword());
        assertTrue(token.isUsed());
        assertThrows(BusinessRuleException.class, () -> service.reset("abc", "another123"));
    }

    @Test
    void expiredLinkIsRejected() {
        PasswordResetToken token = new PasswordResetToken("abc", user, LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));

        assertThrows(BusinessRuleException.class, () -> service.reset("abc", "newsecret1"));
        assertEquals("hash-old", user.getPassword());
    }
}
