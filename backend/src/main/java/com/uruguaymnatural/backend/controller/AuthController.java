package com.uruguaymnatural.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.LoginDto;
import com.uruguaymnatural.backend.dto.PasswordResetConfirmDto;
import com.uruguaymnatural.backend.dto.PasswordResetRequestDto;
import com.uruguaymnatural.backend.dto.SessionDto;
import com.uruguaymnatural.backend.dto.UserDto;
import com.uruguaymnatural.backend.security.AuthUser;
import com.uruguaymnatural.backend.service.AuthService;
import com.uruguaymnatural.backend.service.PasswordResetService;

import jakarta.validation.Valid;

/**
 * Login and password recovery, for the three kinds of user.
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    /** POST /api/auth/login — checks email and password (CU-04). 200 with the token, 401 if wrong. */
    @PostMapping("/auth/login")
    public SessionDto login(@Valid @RequestBody LoginDto data) {
        return authService.login(data);
    }

    /** GET /api/account/me — who is logged in. 401 without a valid token. */
    @GetMapping("/account/me")
    public UserDto me(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user) {
        return authService.getUser(user.id());
    }

    /** POST /api/auth/password-reset — sends the email with the link (CU-05). 204, or 404 if no account. */
    @PostMapping("/auth/password-reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto data) {
        passwordResetService.requestReset(data.email());
    }

    /** POST /api/auth/password-reset/confirm — saves the new password (CU-05). 204, or 409 if the link expired. */
    @PostMapping("/auth/password-reset/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmDto data) {
        passwordResetService.reset(data.token(), data.password());
    }
}
