package com.uruguaymnatural.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.TouristProfileDto;
import com.uruguaymnatural.backend.dto.TouristProfileUpdateDto;
import com.uruguaymnatural.backend.dto.TouristRegistrationDto;
import com.uruguaymnatural.backend.security.AuthUser;
import com.uruguaymnatural.backend.service.TouristService;

import jakarta.validation.Valid;

/**
 * Tourist sign-up (public) and the tourist's own profile (only for the logged-in tourist).
 */
@RestController
@RequestMapping("/api")
public class TouristController {

    private final TouristService touristService;

    public TouristController(TouristService touristService) {
        this.touristService = touristService;
    }

    /** POST /api/tourists — creates a tourist account (CU-03). Answers 201. */
    @PostMapping("/tourists")
    @ResponseStatus(HttpStatus.CREATED)
    public TouristProfileDto register(@Valid @RequestBody TouristRegistrationDto data) {
        return touristService.register(data);
    }

    /** GET /api/tourist/profile — the logged-in tourist's data. */
    @GetMapping("/tourist/profile")
    public TouristProfileDto getProfile(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user) {
        return touristService.getProfile(user.id());
    }

    /** PUT /api/tourist/profile — changes the logged-in tourist's data (CU-06). */
    @PutMapping("/tourist/profile")
    public TouristProfileDto updateProfile(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user,
            @Valid @RequestBody TouristProfileUpdateDto data) {
        return touristService.updateProfile(user.id(), data);
    }
}
