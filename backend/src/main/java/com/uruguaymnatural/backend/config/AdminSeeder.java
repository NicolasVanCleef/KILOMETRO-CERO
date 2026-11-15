package com.uruguaymnatural.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.UserRepository;

/**
 * Creates the first administrator when the backend starts and there is none.
 * Administrators are Uruguay Mnatural staff: they do not sign up from the page.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${admin.email}") String email, @Value("${admin.password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        userRepository.save(new User(email.toLowerCase(), passwordEncoder.encode(password), Role.ADMIN, true));
        log.info("Se creó el administrador inicial: {}", email);
    }
}
