package com.uruguaymnatural.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One attempt to pay a booking (PAGO in the MER). A booking can have several: each failed
 * attempt is kept, and the booking is confirmed only by an approved one (RN-19).
 *
 * The full card number is never stored: only the last 4 digits (RN-24).
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The column allows empty values only because of the test payments made before bookings
    // existed; every new payment belongs to a booking.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    /** The saved card used, if the tourist paid with one. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, length = 4)
    private String cardLast4;

    /** Transaction id given by the payment service (only when approved). */
    private String externalReference;

    /** Why it was not approved, in words for the user. */
    private String message;

    /** Money given back, when the booking was cancelled with a refund. */
    @Column(precision = 12, scale = 2)
    private BigDecimal refundedAmount;

    private LocalDateTime refundedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Payment() {
        // required by JPA
    }

    public Payment(Booking booking, Card card, BigDecimal amount, PaymentStatus status, String cardLast4,
            String externalReference, String message) {
        this.booking = booking;
        this.card = card;
        this.amount = amount;
        this.status = status;
        this.cardLast4 = cardLast4;
        this.externalReference = externalReference;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    /** Records that the payment service gave this much back. */
    public void markRefunded(BigDecimal refunded) {
        this.status = PaymentStatus.REFUNDED;
        this.refundedAmount = refunded;
        this.refundedAt = LocalDateTime.now();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Long getId() {
        return id;
    }

    public Booking getBooking() {
        return booking;
    }

    public Card getCard() {
        return card;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getCardLast4() {
        return cardLast4;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
