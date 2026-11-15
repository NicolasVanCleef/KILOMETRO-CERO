package com.uruguaymnatural.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.BusinessDetailDto;
import com.uruguaymnatural.backend.dto.BusinessProfileUpdateDto;
import com.uruguaymnatural.backend.security.AuthUser;
import com.uruguaymnatural.backend.service.BusinessService;

import jakarta.validation.Valid;

/**
 * The provider's own space to manage the data of their business (RF-33, CU-06).
 */
@RestController
@RequestMapping("/api/provider/business")
public class ProviderBusinessController {

    private final BusinessService businessService;

    public ProviderBusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    /** GET /api/provider/business — the logged-in provider's business. */
    @GetMapping
    public BusinessDetailDto getOwn(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user) {
        return businessService.getOwn(user.id());
    }

    /** PUT /api/provider/business — changes contact, location and description. */
    @PutMapping
    public BusinessDetailDto updateOwn(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user,
            @Valid @RequestBody BusinessProfileUpdateDto data) {
        return businessService.updateOwn(user.id(), data);
    }
}
