package com.uruguaymnatural.backend.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
 * A concrete date and time of an activity, with its own places and price
 * (INSTANCIA_ACTIVIDAD in the MER).
 */
@Entity
@Table(name = "activity_schedules")
public class ActivitySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private int totalCapacity;

    /** Places still free. It goes down when someone books and never below zero (RN-14). */
    @Column(nullable = false)
    private int availableCapacity;

    /** Price per person on this date. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status;

    protected ActivitySchedule() {
        // required by JPA
    }

    public ActivitySchedule(Activity activity, LocalDate date, LocalTime startTime, int capacity, BigDecimal price) {
        this.activity = activity;
        this.date = date;
        this.startTime = startTime;
        this.totalCapacity = capacity;
        this.availableCapacity = capacity;
        this.price = price;
        this.status = ScheduleStatus.SCHEDULED;
    }

    public LocalDateTime getStart() {
        return LocalDateTime.of(date, startTime);
    }

    public boolean isScheduled() {
        return status == ScheduleStatus.SCHEDULED;
    }

    public void cancel() {
        this.status = ScheduleStatus.CANCELLED;
    }

    /** Takes places for a booking. The caller checks first that there are enough. */
    public void takePlaces(int places) {
        if (places > availableCapacity) {
            throw new IllegalStateException("Not enough places");
        }
        this.availableCapacity -= places;
    }

    /** Gives places back when a booking is cancelled or expires. */
    public void releasePlaces(int places) {
        this.availableCapacity = Math.min(totalCapacity, availableCapacity + places);
    }

    public Long getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public int getTotalCapacity() {
        return totalCapacity;
    }

    public int getAvailableCapacity() {
        return availableCapacity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public ScheduleStatus getStatus() {
        return status;
    }
}
