package com.uruguaymnatural.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * One payment attempt (PAGO in the MER). The link to the booking (reserva_id) is added
 * together with bookings (Part 7).
 *
 * The full card number is never stored: only the last 4 digits and the token the payment
 * service returns (RN-24).
 */
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, length = 4)
    private String cardLast4;

    /** Token of the card given by the payment service (only when approved). */
    private String cardToken;

    /** Transaction id given by the payment service (only when approved). */
    private String externalReference;

    /** Why it was not approved, in words for the user. */
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Payment() {
        // required by JPA
    }

    public Payment(BigDecimal amount, PaymentStatus status, String cardLast4, String cardToken,
            String externalReference, String message) {
        this.amount = amount;
        this.status = status;
        this.cardLast4 = cardLast4;
        this.cardToken = cardToken;
        this.externalReference = externalReference;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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

    public String getCardToken() {
        return cardToken;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
