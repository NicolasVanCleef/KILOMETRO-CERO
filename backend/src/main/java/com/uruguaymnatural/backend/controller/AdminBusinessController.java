package com.uruguaymnatural.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.BusinessDetailDto;
import com.uruguaymnatural.backend.dto.BusinessSummaryDto;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.service.BusinessService;

/**
 * Administrator endpoints to review new businesses (CU-02).
 * They will require an administrator login once users and roles are added (Part 4).
 */
@RestController
@RequestMapping("/api/admin/businesses")
public class AdminBusinessController {

    private final BusinessService businessService;

    public AdminBusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    /** GET /api/admin/businesses?status=PENDING — businesses in a status (pending by default). */
    @GetMapping
    public List<BusinessSummaryDto> list(@RequestParam(defaultValue = "PENDING") BusinessStatus status) {
        return businessService.listByStatus(status);
    }

    /** GET /api/admin/businesses/{id} — full data of one business. */
    @GetMapping("/{id}")
    public BusinessDetailDto getDetail(@PathVariable Long id) {
        return businessService.getDetail(id);
    }

    /** POST /api/admin/businesses/{id}/approve — approves a pending business. */
    @PostMapping("/{id}/approve")
    public BusinessDetailDto approve(@PathVariable Long id) {
        return businessService.approve(id);
    }

    /** POST /api/admin/businesses/{id}/reject — rejects a pending business. */
    @PostMapping("/{id}/reject")
    public BusinessDetailDto reject(@PathVariable Long id) {
        return businessService.reject(id);
    }
}
