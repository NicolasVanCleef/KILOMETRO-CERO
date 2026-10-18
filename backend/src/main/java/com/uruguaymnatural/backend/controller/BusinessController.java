package com.uruguaymnatural.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.BusinessDetailDto;
import com.uruguaymnatural.backend.dto.BusinessRegistrationDto;
import com.uruguaymnatural.backend.model.Departments;
import com.uruguaymnatural.backend.service.BusinessService;

import jakarta.validation.Valid;

/**
 * Public endpoints: a provider registers their business.
 */
@RestController
@RequestMapping("/api")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    /** POST /api/businesses — registers a business (CU-01). Answers 201 with the saved data. */
    @PostMapping("/businesses")
    @ResponseStatus(HttpStatus.CREATED)
    public BusinessDetailDto register(@Valid @RequestBody BusinessRegistrationDto data) {
        return businessService.register(data);
    }

    /** GET /api/departments — the 19 departments, for the form's dropdown. */
    @GetMapping("/departments")
    public List<String> getDepartments() {
        return Departments.ALL;
    }
}
