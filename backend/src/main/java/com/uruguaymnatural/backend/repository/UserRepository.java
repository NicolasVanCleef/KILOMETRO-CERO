package com.uruguaymnatural.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);
}
