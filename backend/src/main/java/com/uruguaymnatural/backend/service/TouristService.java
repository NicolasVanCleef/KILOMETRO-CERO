package com.uruguaymnatural.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.TouristProfileDto;
import com.uruguaymnatural.backend.dto.TouristProfileUpdateDto;
import com.uruguaymnatural.backend.dto.TouristRegistrationDto;
import com.uruguaymnatural.backend.exception.BusinessRuleException;
import com.uruguaymnatural.backend.exception.ResourceNotFoundException;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.TouristRepository;
import com.uruguaymnatural.backend.repository.UserRepository;

/**
 * Tourist sign-up (CU-03) and profile (CU-06).
 */
@Service
public class TouristService {

    private final TouristRepository touristRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TouristService(TouristRepository touristRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.touristRepository = touristRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Creates the account. A tourist needs no approval: they can log in right away. */
    @Transactional
    public TouristProfileDto register(TouristRegistrationDto data) {
        String email = data.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("Ya existe una cuenta con el correo " + email + ".");
        }

        User user = new User(email, passwordEncoder.encode(data.password()), Role.TOURIST, true);
        userRepository.save(user);

        Tourist tourist = new Tourist(user, data.firstName().trim(), data.lastName().trim(),
                data.country().trim(), data.birthDate(), data.preferredLanguage().trim(),
                data.document().trim());
        touristRepository.save(tourist);
        return TouristProfileDto.from(tourist);
    }

    @Transactional(readOnly = true)
    public TouristProfileDto getProfile(Long userId) {
        return TouristProfileDto.from(findByUser(userId));
    }

    @Transactional
    public TouristProfileDto updateProfile(Long userId, TouristProfileUpdateDto data) {
        Tourist tourist = findByUser(userId);
        tourist.update(data.firstName().trim(), data.lastName().trim(), data.country().trim(),
                data.birthDate(), data.preferredLanguage().trim(), data.document().trim());
        return TouristProfileDto.from(tourist);
    }

    /** The tourist profile of a logged-in user. Other services use it to know who is booking. */
    public Tourist findByUser(Long userId) {
        return touristRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No encontramos tu perfil de turista."));
    }
}
