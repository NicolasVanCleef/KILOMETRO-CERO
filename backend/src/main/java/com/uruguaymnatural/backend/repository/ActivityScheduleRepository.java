package com.uruguaymnatural.backend.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uruguaymnatural.backend.model.ActivitySchedule;
import com.uruguaymnatural.backend.model.ScheduleStatus;

public interface ActivityScheduleRepository extends JpaRepository<ActivitySchedule, Long> {

    List<ActivitySchedule> findByActivityIdOrderByDateAscStartTimeAsc(Long activityId);

    List<ActivitySchedule> findByActivityIdAndStatus(Long activityId, ScheduleStatus status);

    boolean existsByActivityIdAndDateAndStartTimeAndStatus(Long activityId, LocalDate date, LocalTime startTime,
            ScheduleStatus status);
}
