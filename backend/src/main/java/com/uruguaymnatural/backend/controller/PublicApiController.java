package com.uruguaymnatural.backend.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.ActivitySummaryDto;
import com.uruguaymnatural.backend.dto.PublicApiDto.BusinessItem;
import com.uruguaymnatural.backend.dto.PublicApiDto.DayActivity;
import com.uruguaymnatural.backend.dto.PublicApiDto.DepartmentTop;
import com.uruguaymnatural.backend.dto.PublicApiDto.Verification;
import com.uruguaymnatural.backend.service.PublicApiService;

/**
 * Public REST API for other platforms (CU-25, RF-48 to RF-52). No login needed; it only reads.
 * Documented in docs/API.md.
 */
@RestController
@RequestMapping("/api/public")
public class PublicApiController {

    private final PublicApiService publicApiService;

    public PublicApiController(PublicApiService publicApiService) {
        this.publicApiService = publicApiService;
    }

    /** GET /api/public/businesses — every approved business (RF-48). */
    @GetMapping("/businesses")
    public List<BusinessItem> listBusinesses() {
        return publicApiService.listBusinesses();
    }

    /**
     * GET /api/public/activities?date=2026-12-05&department=Rocha&locality=La%20Paloma — the activities
     * of a day in a department (RF-49). date and department are required; locality is optional.
     */
    @GetMapping("/activities")
    public List<DayActivity> listActivitiesOfDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String department,
            @RequestParam(required = false) String locality) {
        return publicApiService.listActivitiesOfDay(date, department, locality);
    }

    /**
     * GET /api/public/activities/top-rated?year=2026&month=11 — the 3 best rated activities of the
     * month in each department (RF-50). Without year and month it answers for the current month.
     */
    @GetMapping("/activities/top-rated")
    public List<DepartmentTop> topRated(@RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        LocalDate today = LocalDate.now();
        return publicApiService.topRatedOfMonth(year != null ? year : today.getYear(),
                month != null ? month : today.getMonthValue());
    }

    /** GET /api/public/activities/search?q=kayak — activities by keyword (RF-52). */
    @GetMapping("/activities/search")
    public List<ActivitySummaryDto> searchActivities(@RequestParam(name = "q") String keyword) {
        return publicApiService.searchActivities(keyword);
    }

    /** GET /api/public/businesses/{id}/verification — is this business approved? (RF-51) */
    @GetMapping("/businesses/{id}/verification")
    public Verification verifyById(@PathVariable Long id) {
        return publicApiService.verifyById(id);
    }

    /** GET /api/public/businesses/verification?document=217208670014 — the same, by CI or RUT (RF-51). */
    @GetMapping("/businesses/verification")
    public Verification verifyByDocument(@RequestParam String document) {
        return publicApiService.verifyByDocument(document);
    }
}
