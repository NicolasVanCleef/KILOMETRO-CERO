package com.uruguaymnatural.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * A tourist's booking of places on one date of an activity (RESERVA in the MER).
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tourist_id", nullable = false)
    private Tourist tourist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ActivitySchedule schedule;

    @Column(nullable = false)
    private int participantCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    /** Code sent to the tourist by email when the booking is confirmed (RN-23). */
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** While it is pending: until when the places are held. After that it expires. */
    @Column(nullable = false)
    private LocalDateTime paymentDeadline;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingParticipant> participants = new ArrayList<>();

    protected Booking() {
        // required by JPA
    }

    public Booking(Tourist tourist, ActivitySchedule schedule, int participantCount, String code,
            LocalDateTime paymentDeadline) {
        this.tourist = tourist;
        this.schedule = schedule;
        this.participantCount = participantCount;
        this.code = code;
        this.status = BookingStatus.PENDING_PAYMENT;
        this.createdAt = LocalDateTime.now();
        this.paymentDeadline = paymentDeadline;
    }

    /** What the tourist has to pay: the price of the date for each participant. */
    public BigDecimal getTotal() {
        return schedule.getPrice().multiply(BigDecimal.valueOf(participantCount));
    }

    public boolean isFree() {
        return getTotal().signum() == 0;
    }

    /** Pending or confirmed: the booking is still holding its places. */
    public boolean isActive() {
        return status == BookingStatus.PENDING_PAYMENT || status == BookingStatus.CONFIRMED;
    }

    public boolean hasParticipants() {
        return participants.size() == participantCount;
    }

    public void setParticipants(List<BookingParticipant> newParticipants) {
        this.participants.clear();
        this.participants.addAll(newParticipants);
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public void expire() {
        this.status = BookingStatus.EXPIRED;
    }

    public void setPaymentDeadline(LocalDateTime paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public Long getId() {
        return id;
    }

    public Tourist getTourist() {
        return tourist;
    }

    public ActivitySchedule getSchedule() {
        return schedule;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPaymentDeadline() {
        return paymentDeadline;
    }

    public List<BookingParticipant> getParticipants() {
        return participants;
    }
}
