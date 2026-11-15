package com.uruguaymnatural.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByBusinessIdAndActiveTrueOrderByNameAsc(Long businessId);

    /** Is the tag used by an activity that is still published? */
    boolean existsByTagsIdAndActiveTrue(Long tagId);

    List<Activity> findByTagsId(Long tagId);

    /** Is the equipment used by an activity that is still published? */
    boolean existsByEquipmentEquipmentIdAndActiveTrue(Long equipmentId);

    List<Activity> findByEquipmentEquipmentId(Long equipmentId);
}
