package com.uruguaymnatural.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uruguaymnatural.backend.dto.PaymentDto;
import com.uruguaymnatural.backend.dto.PaymentRequestDto;
import com.uruguaymnatural.backend.service.PaymentService;

import jakarta.validation.Valid;

/**
 * Payment endpoints. For now a test amount is paid; once bookings exist (Part 7) each payment
 * belongs to a booking and confirms it.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** POST /api/payments — asks the payment service to charge the card and returns the result. */
    @PostMapping
    public PaymentDto pay(@Valid @RequestBody PaymentRequestDto data) {
        return paymentService.pay(data);
    }

    /** GET /api/payments — the last 20 attempts, newest first. */
    @GetMapping
    public List<PaymentDto> latest() {
        return paymentService.latest();
    }
}
