package com.uruguaymnatural.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A card a tourist saved to pay again later (TARJETA in the MER).
 *
 * The full number is never stored (RN-24): only the token the payment service gave for it,
 * which is what is sent to charge it again, and the last 4 digits to show the tourist which one it is.
 */
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

    @Column(nullable = false)
    private String gatewayToken;

    @Column(nullable = false, length = 4)
    private String last4;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Card() {
        // required by JPA
    }

    public Card(Tourist tourist, String gatewayToken, String last4) {
        this.tourist = tourist;
        this.gatewayToken = gatewayToken;
        this.last4 = last4;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Tourist getTourist() {
        return tourist;
    }

    public String getGatewayToken() {
        return gatewayToken;
    }

    public String getLast4() {
        return last4;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
