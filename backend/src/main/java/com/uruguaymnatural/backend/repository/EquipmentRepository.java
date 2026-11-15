package com.uruguaymnatural.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findAllByOrderByNameAsc();

    Optional<Equipment> findByNameIgnoreCase(String name);
}
