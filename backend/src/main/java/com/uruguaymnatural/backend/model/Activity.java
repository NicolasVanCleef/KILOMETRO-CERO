package com.uruguaymnatural.backend.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * An activity offered by a business (ACTIVIDAD in the MER). It describes what is offered;
 * the concrete dates are {@link ActivitySchedule}.
 */
@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    /** For example the minimum age. */
    @Column(length = 1000)
    private String requirements;

    @Column(length = 1000)
    private String observations;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String locality;

    private String address;

    private Double latitude;

    private Double longitude;

    @Column(nullable = false)
    private int durationMinutes;

    /** Default places of each date; a date can have its own (RN-08). */
    @Column(nullable = false)
    private int defaultCapacity;

    /** Default price per person; 0 means the activity is free (RN-09). */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal defaultPrice;

    /** Bookings close this many minutes before the start; 0 = until the start (RN-10). */
    @Column(nullable = false)
    private int minBookingMinutes;

    /** Bookings open this many minutes before the start; empty = no limit. */
    private Integer maxBookingMinutes;

    /** Cancellation policy chosen by the provider (RN-25). */
    @Column(nullable = false)
    private boolean cancellable;

    /** The tourist can cancel until this many minutes before the start. Only if cancellable. */
    private Integer cancellationMinutes;

    @Column(nullable = false)
    private boolean refundable;

    /** Percentage given back to the tourist who cancels, 0 to 100. Only if refundable. */
    private Integer refundPercentage;

    /** false = deleted by the provider or the administrator. Kept so past bookings still make sense. */
    @Column(nullable = false)
    private boolean active;

    @ManyToMany
    @JoinTable(name = "activity_tags",
            joinColumns = @JoinColumn(name = "activity_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new LinkedHashSet<>();

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivityEquipment> equipment = new ArrayList<>();

    protected Activity() {
        // required by JPA
    }

    public Activity(Business business) {
        this.business = business;
        this.active = true;
    }

    public void setDescriptionData(String name, String description, String requirements, String observations,
            int durationMinutes) {
        this.name = name;
        this.description = description;
        this.requirements = requirements;
        this.observations = observations;
        this.durationMinutes = durationMinutes;
    }

    public void setLocation(String department, String locality, String address, Double latitude, Double longitude) {
        this.department = department;
        this.locality = locality;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setBookingRules(int defaultCapacity, BigDecimal defaultPrice, int minBookingMinutes,
            Integer maxBookingMinutes) {
        this.defaultCapacity = defaultCapacity;
        this.defaultPrice = defaultPrice;
        this.minBookingMinutes = minBookingMinutes;
        this.maxBookingMinutes = maxBookingMinutes;
    }

    public void setCancellationPolicy(boolean cancellable, Integer cancellationMinutes, boolean refundable,
            Integer refundPercentage) {
        this.cancellable = cancellable;
        this.cancellationMinutes = cancellationMinutes;
        this.refundable = refundable;
        this.refundPercentage = refundPercentage;
    }

    public void setTags(Set<Tag> newTags) {
        this.tags.clear();
        this.tags.addAll(newTags);
    }

    /**
     * Replaces the equipment list of the activity. The rows of equipment that stays are kept
     * (only whether it is required or provided may change): deleting and re-adding the same
     * equipment in one save breaks the unique (activity, equipment) rule, because the database
     * receives the new rows before the deleted ones.
     */
    public void setEquipment(List<Equipment> required, List<Equipment> provided) {
        Map<Long, EquipmentRelation> wanted = new LinkedHashMap<>();
        required.forEach(item -> wanted.put(item.getId(), EquipmentRelation.REQUIRED));
        provided.forEach(item -> wanted.put(item.getId(), EquipmentRelation.PROVIDED));

        this.equipment.removeIf(row -> !wanted.containsKey(row.getEquipment().getId()));
        this.equipment.forEach(row -> row.setRelation(wanted.remove(row.getEquipment().getId())));
        Stream.concat(required.stream(), provided.stream())
                .filter(item -> wanted.containsKey(item.getId()))
                .forEach(item -> this.equipment.add(new ActivityEquipment(this, item, wanted.get(item.getId()))));
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isFree() {
        return defaultPrice.signum() == 0;
    }

    public Long getId() {
        return id;
    }

    public Business getBusiness() {
        return business;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getRequirements() {
        return requirements;
    }

    public String getObservations() {
        return observations;
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

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getDefaultCapacity() {
        return defaultCapacity;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public int getMinBookingMinutes() {
        return minBookingMinutes;
    }

    public Integer getMaxBookingMinutes() {
        return maxBookingMinutes;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public Integer getCancellationMinutes() {
        return cancellationMinutes;
    }

    public boolean isRefundable() {
        return refundable;
    }

    public Integer getRefundPercentage() {
        return refundPercentage;
    }

    public boolean isActive() {
        return active;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public List<ActivityEquipment> getEquipment() {
        return equipment;
    }
}
