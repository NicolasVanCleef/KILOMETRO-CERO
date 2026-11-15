package com.uruguaymnatural.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.uruguaymnatural.backend.model.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /** What the portal shows: activities not deleted, of businesses approved by the administrator. */
    @Query("""
            select a from Activity a
            where a.active = true and a.business.status = com.uruguaymnatural.backend.model.BusinessStatus.APPROVED
            order by a.name
            """)
    List<Activity> findPublished();

    List<Activity> findByBusinessIdAndActiveTrueOrderByNameAsc(Long businessId);

    /** Is the tag used by an activity that is still published? */
    boolean existsByTagsIdAndActiveTrue(Long tagId);

    List<Activity> findByTagsId(Long tagId);

    /** Is the equipment used by an activity that is still published? */
    boolean existsByEquipmentEquipmentIdAndActiveTrue(Long equipmentId);

    List<Activity> findByEquipmentEquipmentId(Long equipmentId);
}
