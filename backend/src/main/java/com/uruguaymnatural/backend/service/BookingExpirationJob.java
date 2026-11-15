package com.uruguaymnatural.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * The "system clock" actor of CU-17: every so often it looks for pending bookings whose
 * time ran out and releases their places (RNF-13).
 */
@Component
public class BookingExpirationJob {

    private static final Logger log = LoggerFactory.getLogger(BookingExpirationJob.class);

    private final BookingService bookingService;

    public BookingExpirationJob(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Scheduled(fixedDelayString = "${booking.expiration-check-ms}")
    public void run() {
        int expired = bookingService.expireOverdue();
        if (expired > 0) {
            log.info("Reservas vencidas liberadas: {}", expired);
        }
    }
}
