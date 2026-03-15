package com.macromind.macromind_api.repositories;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.macromind.macromind_api.models.MealModel;

public interface MealRepository extends JpaRepository<MealModel, Long> {
    List<MealModel> findByUserId(Long userId);

    List<MealModel> findByUserIdAndMealDate(Long userId, Date mealDate);

    List<MealModel> findByDailyGoalId(Long dailyGoalId);
}
