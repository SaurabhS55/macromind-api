package com.macromind.macromind_api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.macromind.macromind_api.models.MealItemModel;

public interface MealItemRepository extends JpaRepository<MealItemModel, Long> {
    List<MealItemModel> findByMealId(Long mealId);
}
