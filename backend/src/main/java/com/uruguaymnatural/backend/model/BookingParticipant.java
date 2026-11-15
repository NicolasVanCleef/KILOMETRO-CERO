package com.uruguaymnatural.backend.model;

import java.time.LocalDate;

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
 * One person of a booking (PARTICIPANTE in the MER): one row per place (RN-16).
 */
@Entity
@Table(name = "booking_participants")
public class BookingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String document;

    protected BookingParticipant() {
        // required by JPA
    }

    public BookingParticipant(Booking booking, String firstName, String lastName, LocalDate birthDate,
            String document) {
        this.booking = booking;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.document = document;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getDocument() {
        return document;
    }
}
