package com.uruguaymnatural.backend.model;

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
import jakarta.persistence.UniqueConstraint;

/**
 * One piece of equipment of an activity, and whether the tourist brings it or the provider
 * gives it (ACTIVIDAD_EQUIPAMIENTO in the MER). The same equipment cannot be twice in an activity.
 */
@Entity
@Table(name = "activity_equipment",
        uniqueConstraints = @UniqueConstraint(columnNames = { "activity_id", "equipment_id" }))
public class ActivityEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentRelation relation;

    protected ActivityEquipment() {
        // required by JPA
    }

    public ActivityEquipment(Activity activity, Equipment equipment, EquipmentRelation relation) {
        this.activity = activity;
        this.equipment = equipment;
        this.relation = relation;
    }

    public Long getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public Equipment getEquipment() {
        return equipment;
    }

    public EquipmentRelation getRelation() {
        return relation;
    }

    public void setRelation(EquipmentRelation relation) {
        this.relation = relation;
    }
}
