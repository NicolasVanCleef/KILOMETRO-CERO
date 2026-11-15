package com.uruguaymnatural.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Card;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByTouristIdOrderByCreatedAtDesc(Long touristId);
}
