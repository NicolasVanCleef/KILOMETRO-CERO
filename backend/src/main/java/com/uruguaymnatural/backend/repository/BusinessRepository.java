package com.uruguaymnatural.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Business;
import com.uruguaymnatural.backend.model.BusinessStatus;
import com.uruguaymnatural.backend.model.PersonType;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    List<Business> findByStatusOrderByCreatedAtAsc(BusinessStatus status);

    boolean existsByPersonTypeAndDocument(PersonType personType, String document);
}
