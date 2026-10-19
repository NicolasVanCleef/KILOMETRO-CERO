package com.uruguaymnatural.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findTop20ByOrderByCreatedAtDesc();
}
