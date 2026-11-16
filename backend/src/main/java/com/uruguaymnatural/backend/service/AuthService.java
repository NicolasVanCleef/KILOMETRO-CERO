package com.uruguaymnatural.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uruguaymnatural.backend.dto.LoginDto;
import com.uruguaymnatural.backend.dto.SessionDto;
import com.uruguaymnatural.backend.dto.UserDto;
import com.uruguaymnatural.backend.exception.ForbiddenException;
import com.uruguaymnatural.backend.exception.UnauthorizedException;
import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.Tourist;
import com.uruguaymnatural.backend.model.User;
import com.uruguaymnatural.backend.repository.BusinessRepository;
import com.uruguaymnatural.backend.repository.TouristRepository;
import com.uruguaymnatural.backend.repository.UserRepository;
import com.uruguaymnatural.backend.security.JwtService;

/**
 * Login (CU-04). Logging out (CU-07) needs no backend code: the frontend just forgets the token.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TouristRepository touristRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, TouristRepository touristRepository,
            BusinessRepository businessRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.touristRepository = touristRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Checks email and password and answers with a token. A provider whose business is not
     * approved cannot log in (RN-01).
     */
    @Transactional(readOnly = true)
    public SessionDto login(LoginDto data) {
        User user = userRepository.findByEmailIgnoreCase(data.email().trim())
                .filter(found -> passwordEncoder.matches(data.password(), found.getPassword()))
                // Same message for a wrong email and a wrong password, so nobody can find out
                // which emails have an account.
                .orElseThrow(() -> new UnauthorizedException("El correo o la contraseña no son correctos."));

        if (!user.isEnabled()) {
            throw new ForbiddenException(disabledMessage(user));
        }
        return new SessionDto(jwtService.createToken(user), toDto(user));
    }

    @Transactional(readOnly = true)
    public UserDto getUser(Long userId) {
        return userRepository.findById(userId)
                .map(this::toDto)
                .orElseThrow(() -> new UnauthorizedException("Tenés que iniciar sesión."));
    }

    private String disabledMessage(User user) {
        if (user.getRole() == Role.PROVIDER) {
            BusinessStatus status = businessRepository.findByUserId(user.getId())
                    .map(Business::getStatus)
                    .orElse(null);
            if (status == BusinessStatus.PENDING) {
                return "Tu emprendimiento todavía no fue aprobado. Te vamos a avisar por correo cuando un "
                        + "administrador lo revise.";
            }
            if (status == BusinessStatus.REJECTED) {
                return "Tu emprendimiento no fue aprobado, así que no podés iniciar sesión.";
            }
            if (status == BusinessStatus.REMOVED) {
                return "Uruguay Mnatural dio de baja tu emprendimiento, así que no podés iniciar sesión.";
            }
        }
        return "Tu cuenta está deshabilitada.";
    }

    private UserDto toDto(User user) {
        String displayName = switch (user.getRole()) {
            case TOURIST -> touristRepository.findByUserId(user.getId())
                    .map(Tourist::getFirstName).orElse(user.getEmail());
            case PROVIDER -> businessRepository.findByUserId(user.getId())
                    .map(Business::getLegalName).orElse(user.getEmail());
            case ADMIN -> "Administrador";
        };
        return new UserDto(user.getId(), user.getEmail(), user.getRole(), displayName);
    }
}
