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
import jakarta.persistence.UniqueConstraint;

/**
 * A tourist's rating and comment about an activity (VALORACION in the MER). It belongs to
 * the activity, not to one date nor to the business (RN-32). Each tourist has at most one
 * per activity, which they can edit or delete.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(columnNames = { "activity_id", "tourist_id" }))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

    /** From 1 to 5 (RN-33). */
    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, length = 1000)
    private String comment;

    /** What the provider answered, if they did (RN-35: one reply per comment). */
    @Column(length = 1000)
    private String providerReply;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Review() {
        // required by JPA
    }

    public Review(Activity activity, Tourist tourist, int rating, String comment) {
        this.activity = activity;
        this.tourist = tourist;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    public void update(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public void setProviderReply(String providerReply) {
        this.providerReply = providerReply;
    }

    public Long getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public Tourist getTourist() {
        return tourist;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getProviderReply() {
        return providerReply;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
