package com.uruguaymnatural.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Role;
import com.uruguaymnatural.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByRole(Role role);
}
