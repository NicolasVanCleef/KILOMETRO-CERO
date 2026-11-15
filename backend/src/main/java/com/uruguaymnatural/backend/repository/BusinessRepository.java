package com.uruguaymnatural.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.model.PersonType;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByUserId(Long userId);

    List<Business> findByStatusOrderByCreatedAtAsc(BusinessStatus status);

    Optional<Business> findFirstByDocument(String document);

    boolean existsByPersonTypeAndDocument(PersonType personType, String document);
}
