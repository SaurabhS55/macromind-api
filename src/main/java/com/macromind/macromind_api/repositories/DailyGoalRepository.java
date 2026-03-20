package com.macromind.macromind_api.repositories;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.macromind.macromind_api.models.DailyGoalModel;

public interface DailyGoalRepository extends JpaRepository<DailyGoalModel, Long> {
    List<DailyGoalModel> findByUserId(Long userId);

    Optional<DailyGoalModel> findByUserIdAndDate(Long userId, Date date);

    List<DailyGoalModel> findByUserIdAndDateBetween(Long userId, Date startDate, Date endDate);
}
