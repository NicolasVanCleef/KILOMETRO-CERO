package com.uruguaymnatural.backend.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Profile of a tourist (TURISTA in the MER), with the data the client asked for at sign-up.
 */
@Entity
@Table(name = "tourists")
public class Tourist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false)
    private String preferredLanguage;

    @Column(nullable = false)
    private String document;

    protected Tourist() {
        // required by JPA
    }

    public Tourist(User user, String firstName, String lastName, String country, LocalDate birthDate,
            String preferredLanguage, String document) {
        this.user = user;
        update(firstName, lastName, country, birthDate, preferredLanguage, document);
    }

    /** Changes the profile data (CU-06). The email is not part of the profile: it identifies the account. */
    public void update(String firstName, String lastName, String country, LocalDate birthDate,
            String preferredLanguage, String document) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.birthDate = birthDate;
        this.preferredLanguage = preferredLanguage;
        this.document = document;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getCountry() {
        return country;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public String getDocument() {
        return document;
    }
}
