package com.uruguaymnatural.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.BookingPaymentDto;
import com.uruguaymnatural.backend.dto.BookingPaymentRequestDto;
import com.uruguaymnatural.backend.dto.CardDto;
import com.uruguaymnatural.backend.security.AuthUser;
import com.uruguaymnatural.backend.service.PaymentService;

/**
 * The tourist pays their bookings (CU-16).
 */
@RestController
@RequestMapping("/api/tourist")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /api/tourist/bookings/{id}/payments — tries to pay a pending booking with a new or
     * a saved card. Answers 201 with the attempt and the booking: confirmed if the payment was
     * approved, still pending if it was rejected or the payment service failed.
     */
    @PostMapping("/bookings/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public BookingPaymentDto pay(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user, @PathVariable Long id,
            @RequestBody BookingPaymentRequestDto data) {
        return paymentService.payBooking(user.id(), id, data);
    }

    /** GET /api/tourist/cards — the logged-in tourist's saved cards (only the last 4 digits). */
    @GetMapping("/cards")
    public List<CardDto> listCards(@RequestAttribute(AuthUser.ATTRIBUTE) AuthUser user) {
        return paymentService.listCards(user.id());
    }
}
