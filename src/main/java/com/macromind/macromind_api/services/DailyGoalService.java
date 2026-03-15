package com.macromind.macromind_api.services;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.macromind.macromind_api.dtos.DailyGoalHeatmapResponse;
import com.macromind.macromind_api.dtos.DailyGoalRequest;
import com.macromind.macromind_api.dtos.DailyGoalResponse;
import com.macromind.macromind_api.dtos.MealItemResponse;
import com.macromind.macromind_api.dtos.MealResponse;
import com.macromind.macromind_api.enums.DailyGoalStatus;
import com.macromind.macromind_api.enums.FitnessGoalStatus;
import com.macromind.macromind_api.enums.MealStatus;
import com.macromind.macromind_api.exceptions.ResourceNotFoundException;
import com.macromind.macromind_api.models.DailyGoalModel;
import com.macromind.macromind_api.models.FitnessGoalModel;
import com.macromind.macromind_api.models.MealModel;
import com.macromind.macromind_api.repositories.DailyGoalRepository;
import com.macromind.macromind_api.repositories.FitnessGoalRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyGoalService {

    private final DailyGoalRepository dailyGoalRepository;
    private final FitnessGoalRepository fitnessGoalRepository;

    public DailyGoalResponse createOrGetDailyGoal(Long userId, DailyGoalRequest request) {
        // Check if a daily goal already exists for this date
        return dailyGoalRepository.findByUserIdAndDate(userId, request.getDate())
                .map(this::mapToResponse)
                .orElseGet(() -> {
                    // Get active fitness goal for target values
                    FitnessGoalModel activeGoal = fitnessGoalRepository
                            .findByUserIdAndStatus(userId, FitnessGoalStatus.ACTIVE)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "No active fitness goal found. Please create a fitness goal first."));

                    DailyGoalModel dailyGoal = DailyGoalModel.builder()
                            .user(activeGoal.getUser())
                            .date(request.getDate())
                            .targetCalories(activeGoal.getTargetCalories())
                            .targetProtein(activeGoal.getTargetProtein())
                            .targetCarbs(activeGoal.getTargetCarbs())
                            .targetFats(activeGoal.getTargetFats())
                            .consumedCalories(0.0)
                            .consumedProtein(0.0)
                            .consumedCarbs(0.0)
                            .consumedFats(0.0)
                            .status(DailyGoalStatus.UNDER_CONSUMPTION)
                            .build();

                    DailyGoalModel saved = dailyGoalRepository.save(dailyGoal);
                    return mapToResponse(saved);
                });
    }

    public DailyGoalResponse getDailyGoal(Long userId, Date date) {
        DailyGoalModel goal = dailyGoalRepository.findByUserIdAndDate(userId, date)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No daily goal found for date: " + date));
        return mapToResponse(goal);
    }

    public DailyGoalResponse getTodayGoal(Long userId) {
        Date today = new Date(System.currentTimeMillis());
        return dailyGoalRepository.findByUserIdAndDate(userId, today)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No daily goal found for today. Create one first."));
    }

    public List<DailyGoalResponse> getDailyGoalsByUser(Long userId) {
        return dailyGoalRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DailyGoalHeatmapResponse> getYearlyHeatmap(Long userId, int year) {
        Date startDate = Date.valueOf(year + "-01-01");
        Date endDate = Date.valueOf(year + "-12-31");

        List<DailyGoalModel> goals = dailyGoalRepository
                .findByUserIdAndDateBetween(userId, startDate, endDate);

        return goals.stream().map(goal -> {
            double calPct = safePercentage(goal.getConsumedCalories(), goal.getTargetCalories());
            double proPct = safePercentage(goal.getConsumedProtein(), goal.getTargetProtein());
            double carbPct = safePercentage(goal.getConsumedCarbs(), goal.getTargetCarbs());
            double fatPct = safePercentage(goal.getConsumedFats(), goal.getTargetFats());

            return DailyGoalHeatmapResponse.builder()
                    .date(goal.getDate())
                    .status(goal.getStatus())
                    .caloriePercentage(Math.round(calPct * 100.0) / 100.0)
                    .proteinPercentage(Math.round(proPct * 100.0) / 100.0)
                    .carbsPercentage(Math.round(carbPct * 100.0) / 100.0)
                    .fatsPercentage(Math.round(fatPct * 100.0) / 100.0)
                    .build();
        }).collect(Collectors.toList());
    }

    public void updateConsumedNutrients(Long dailyGoalId) {
        DailyGoalModel dailyGoal = dailyGoalRepository.findById(dailyGoalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Daily goal not found with id: " + dailyGoalId));

        // Sum nutrients from COMPLETED meals only
        List<MealModel> meals = dailyGoal.getMeals();
        if (meals == null)
            meals = new ArrayList<>();

        double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFats = 0;
        for (MealModel meal : meals) {
            if (meal.getStatus() == MealStatus.COMPLETED) {
                totalCalories += meal.getTotalCalories() != null ? meal.getTotalCalories() : 0;
                totalProtein += meal.getTotalProtein() != null ? meal.getTotalProtein() : 0;
                totalCarbs += meal.getTotalCarbs() != null ? meal.getTotalCarbs() : 0;
                totalFats += meal.getTotalFats() != null ? meal.getTotalFats() : 0;
            }
        }

        dailyGoal.setConsumedCalories(totalCalories);
        dailyGoal.setConsumedProtein(totalProtein);
        dailyGoal.setConsumedCarbs(totalCarbs);
        dailyGoal.setConsumedFats(totalFats);

        // Compute status
        dailyGoal.setStatus(computeStatus(dailyGoal));
        dailyGoalRepository.save(dailyGoal);
    }

    DailyGoalStatus computeStatus(DailyGoalModel goal) {
        if (goal.getTargetCalories() == null || goal.getTargetCalories() == 0) {
            return DailyGoalStatus.ON_TRACK;
        }

        double consumed = goal.getConsumedCalories() != null ? goal.getConsumedCalories() : 0;
        double target = goal.getTargetCalories();
        double ratio = consumed / target;

        if (ratio > 1.05) {
            return DailyGoalStatus.OVER_CONSUMPTION;
        } else if (ratio < 0.80) {
            return DailyGoalStatus.UNDER_CONSUMPTION;
        } else {
            return DailyGoalStatus.ON_TRACK;
        }
    }

    private double safePercentage(Double consumed, Double target) {
        if (target == null || target == 0)
            return 0.0;
        if (consumed == null)
            return 0.0;
        return (consumed / target) * 100.0;
    }

    private DailyGoalResponse mapToResponse(DailyGoalModel goal) {
        List<MealResponse> mealResponses = null;
        if (goal.getMeals() != null) {
            mealResponses = goal.getMeals().stream()
                    .map(this::mapMealToResponse)
                    .collect(Collectors.toList());
        }

        return DailyGoalResponse.builder()
                .id(goal.getId())
                .userId(goal.getUser().getId())
                .date(goal.getDate())
                .targetCalories(goal.getTargetCalories())
                .targetProtein(goal.getTargetProtein())
                .targetCarbs(goal.getTargetCarbs())
                .targetFats(goal.getTargetFats())
                .consumedCalories(goal.getConsumedCalories())
                .consumedProtein(goal.getConsumedProtein())
                .consumedCarbs(goal.getConsumedCarbs())
                .consumedFats(goal.getConsumedFats())
                .status(goal.getStatus())
                .meals(mealResponses)
                .build();
    }

    private MealResponse mapMealToResponse(MealModel meal) {
        List<MealItemResponse> itemResponses = null;
        if (meal.getMealItems() != null) {
            itemResponses = meal.getMealItems().stream()
                    .map(item -> MealItemResponse.builder()
                            .id(item.getId())
                            .itemName(item.getItemName())
                            .calories(item.getCalories())
                            .protein(item.getProtein())
                            .carbs(item.getCarbs())
                            .fats(item.getFats())
                            .quantity(item.getQuantity())
                            .build())
                    .collect(Collectors.toList());
        }

        return MealResponse.builder()
                .id(meal.getId())
                .userId(meal.getUser().getId())
                .dailyGoalId(meal.getDailyGoal() != null ? meal.getDailyGoal().getId() : null)
                .mealDate(meal.getMealDate())
                .status(meal.getStatus())
                .totalCalories(meal.getTotalCalories())
                .totalProtein(meal.getTotalProtein())
                .totalCarbs(meal.getTotalCarbs())
                .totalFats(meal.getTotalFats())
                .mealItems(itemResponses)
                .createdAt(meal.getCreatedAt())
                .consumedAt(meal.getConsumedAt())
                .build();
    }
}
