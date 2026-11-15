package com.uruguaymnatural.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A type of equipment, for example "Casco" (EQUIPAMIENTO in the MER).
 * Only the administrator manages them (RN-06). Each activity then says whether the tourist
 * has to bring it or the provider gives it.
 */
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    protected Equipment() {
        // required by JPA
    }

    public Equipment(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
