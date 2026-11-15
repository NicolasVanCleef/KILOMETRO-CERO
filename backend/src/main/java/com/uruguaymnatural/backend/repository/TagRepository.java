package com.uruguaymnatural.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findAllByOrderByNameAsc();

    Optional<Tag> findByNameIgnoreCase(String name);
}
