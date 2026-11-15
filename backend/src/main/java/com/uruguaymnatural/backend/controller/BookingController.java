package com.uruguaymnatural.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.BookingDto;
import com.uruguaymnatural.backend.dto.BookingRequestDto;
import com.uruguaymnatural.backend.dto.ParticipantsRequestDto;
import com.uruguaymnatural.backend.dto.ScheduleBookingsDto;
import com.uruguaymnatural.backend.security.AuthUser;
import com.uruguaymnatural.backend.service.BookingService;

import jakarta.validation.Valid;

/**
 * Bookings: the tourist makes and manages their own; the provider sees the ones of their dates.
 */
@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /** POST /api/tourist/bookings — holds places on a date for 10 minutes (CU-15). 201, or 409 if there are not enough. */
    @PostMapping("/tourist/bookings")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto hold(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user,
            @Valid @RequestBody BookingRequestDto data) {
        return bookingService.hold(user.id(), data);
    }

    /** PUT /api/tourist/bookings/{id}/participants — saves the data of each participant. */
    @PutMapping("/tourist/bookings/{id}/participants")
    public BookingDto saveParticipants(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id,
            @Valid @RequestBody ParticipantsRequestDto data) {
        return bookingService.saveParticipants(user.id(), id, data.participants());
    }

    /** GET /api/tourist/bookings — the logged-in tourist's bookings, newest first (CU-18). */
    @GetMapping("/tourist/bookings")
    public List<BookingDto> listOwn(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user) {
        return bookingService.listOwn(user.id());
    }

    /** GET /api/tourist/bookings/{id} — one booking of the logged-in tourist. */
    @GetMapping("/tourist/bookings/{id}")
    public BookingDto getOwn(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id) {
        return bookingService.getOwn(user.id(), id);
    }

    /** POST /api/tourist/bookings/{id}/cancel — the tourist cancels a booking (CU-19). 409 if not allowed. */
    @PostMapping("/tourist/bookings/{id}/cancel")
    public BookingDto cancelOwn(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id) {
        return bookingService.cancelOwn(user.id(), id);
    }

    /** GET /api/provider/schedules/{id}/bookings — the bookings of one date of the provider (CU-22). */
    @GetMapping("/provider/schedules/{id}/bookings")
    public ScheduleBookingsDto listOfSchedule(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user,
            @PathVariable Long id) {
        return bookingService.listOfSchedule(user.id(), id);
    }
}
