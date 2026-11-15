package com.uruguaymnatural.backend.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.PasswordResetToken;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.PasswordResetTokenRepository;
import com.uruguaymnatural.backend.repository.UserRepository;

/**
 * Recover password (CU-05, RF-53): an email with a link, and the link lets you set a new one.
 */
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String frontendUrl;
    private final long validMinutes;

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder, EmailService emailService,
            @Value("${frontend.url}") String frontendUrl,
            @Value("${password-reset.minutes}") long validMinutes) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.frontendUrl = frontendUrl;
        this.validMinutes = validMinutes;
    }

    /** Sends the email with the link. If there is no account with that email, it says so (CU-05, 3a). */
    @Transactional
    public void requestReset(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No hay una cuenta con ese correo."));

        String token = UUID.randomUUID().toString();
        tokenRepository.save(new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(validMinutes)));

        emailService.send(user.getEmail(),
                "Cambiá tu contraseña",
                "Hola. Para poner una contraseña nueva entrá a este enlace:\n"
                        + frontendUrl + "/restablecer?token=" + token + "\n"
                        + "El enlace vale por " + validMinutes + " minutos y se puede usar una sola vez.\n"
                        + "Si no pediste cambiar la contraseña, no hagas nada.");
    }

    /** Saves the new password if the link is still valid (CU-05, 5a). */
    @Transactional
    public void reset(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .filter(found -> found.isUsable(LocalDateTime.now()))
                .orElseThrow(() -> new BusinessRuleException(
                        "El enlace ya venció o ya se usó. Pedí uno nuevo desde \"Recuperar contraseña\"."));

        resetToken.getUser().setPassword(passwordEncoder.encode(newPassword));
        resetToken.markUsed();
    }
}
