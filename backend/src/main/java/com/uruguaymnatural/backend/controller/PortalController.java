package com.uruguaymnatural.backend.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.ActivityDetailDto;
import com.uruguaymnatural.backend.dto.ActivitySearchDto;
import com.uruguaymnatural.backend.dto.ActivitySummaryDto;
import com.uruguaymnatural.backend.service.PortalService;

/**
 * The public portal (no login needed, RF-01).
 */
@RestController
@RequestMapping("/api/activities")
public class PortalController {

    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    /**
     * GET /api/activities?department=Rocha&locality=...&tagId=3&date=2026-12-05&maxPrice=2000&q=kayak
     * — searches published activities (CU-13). Every filter is optional.
     */
    @GetMapping
    public List<ActivitySummaryDto> search(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(name = "q", required = false) String keyword) {
        return portalService.search(new ActivitySearchDto(department, locality, tagId, date, maxPrice, keyword));
    }

    /** GET /api/activities/{id} — the detail of an activity with its coming dates (CU-14). 404 if not published. */
    @GetMapping("/{id}")
    public ActivityDetailDto getDetail(@PathVariable Long id) {
        return portalService.getDetail(id);
    }
}
