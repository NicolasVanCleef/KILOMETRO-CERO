package com.uruguaymnatural.backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.ScheduleStatus;

import jakarta.persistence.LockModeType;

public interface ActivityScheduleRepository extends JpaRepository<ActivitySchedule, Long> {

    /**
     * Reads a date and locks its row until the transaction ends ("select ... for update").
     * Used whenever places are taken or released, so two bookings at the same time cannot
     * both take the last places (RNF-12).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ActivitySchedule s where s.id = :id")
    Optional<ActivitySchedule> findByIdForUpdate(@Param("id") Long id);

    List<ActivitySchedule> findByActivityIdOrderByDateAscStartTimeAsc(Long activityId);

    List<ActivitySchedule> findByActivityIdAndStatus(Long activityId, ScheduleStatus status);

    List<ActivitySchedule> findByStatusAndDateGreaterThanEqual(ScheduleStatus status, LocalDate date);

    List<ActivitySchedule> findByDateAndStatusOrderByStartTimeAsc(LocalDate date, ScheduleStatus status);

    boolean existsByActivityIdAndDateAndStartTimeAndStatus(Long activityId, LocalDate date, LocalTime startTime,
            ScheduleStatus status);
}
