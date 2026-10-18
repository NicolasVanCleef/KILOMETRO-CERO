package com.uruguaymnatural.backend.model;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * A provider's business (EMPRENDIMIENTO in the MER).
 */
@Entity
@Table(name = "businesses")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PersonType personType;

    /** CI for an individual, RUT for a company. Digits only. */
    @Column(nullable = false)
    private String document;

    /** Razón social. */
    @Column(nullable = false)
    private String legalName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    /** Only for companies. */
    private String representativeName;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String locality;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime reviewedAt;

    protected Business() {
        // required by JPA
    }

    public Business(User user, PersonType personType, String document, String legalName, String phone,
            String email, String representativeName, String description, String department,
            String locality, String address) {
        this.user = user;
        this.personType = personType;
        this.document = document;
        this.legalName = legalName;
        this.phone = phone;
        this.email = email;
        this.representativeName = representativeName;
        this.description = description;
        this.department = department;
        this.locality = locality;
        this.address = address;
        this.status = BusinessStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = BusinessStatus.APPROVED;
        this.reviewedAt = LocalDateTime.now();
        this.user.setEnabled(true);
    }

    public void reject() {
        this.status = BusinessStatus.REJECTED;
        this.reviewedAt = LocalDateTime.now();
        this.user.setEnabled(false);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public String getDocument() {
        return document;
    }

    public String getLegalName() {
        return legalName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public String getDescription() {
        return description;
    }

    public String getDepartment() {
        return department;
    }

    public String getLocality() {
        return locality;
    }

    public String getAddress() {
        return address;
    }

    public BusinessStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}
