package com.uruguaymnatural.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Tourist;

public interface TouristRepository extends JpaRepository<Tourist, Long> {

    Optional<Tourist> findByUserId(Long userId);
}
