package com.macromind.macromind_api.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.macromind.macromind_api.dtos.MealItemRequest;
import com.macromind.macromind_api.dtos.MealItemResponse;
import com.macromind.macromind_api.dtos.MealRequest;
import com.macromind.macromind_api.dtos.MealResponse;
import com.macromind.macromind_api.enums.MealStatus;
import com.macromind.macromind_api.exceptions.ResourceNotFoundException;
import com.macromind.macromind_api.models.DailyGoalModel;
import com.macromind.macromind_api.models.MealItemModel;
import com.macromind.macromind_api.models.MealModel;
import com.macromind.macromind_api.models.UserModel;
import com.macromind.macromind_api.repositories.DailyGoalRepository;
import com.macromind.macromind_api.repositories.MealRepository;
import com.macromind.macromind_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;
    private final UserRepository userRepository;
    private final DailyGoalRepository dailyGoalRepository;
    private final DailyGoalService dailyGoalService;

    @Transactional
    public MealResponse createMeal(Long userId, MealRequest request) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        DailyGoalModel dailyGoal = null;
        if (request.getDailyGoalId() != null) {
            dailyGoal = dailyGoalRepository.findById(request.getDailyGoalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Daily goal not found with id: " + request.getDailyGoalId()));
        }

        MealModel meal = MealModel.builder()
                .user(user)
                .dailyGoal(dailyGoal)
                .mealDate(request.getMealDate())
                .status(MealStatus.DRAFT)
                .totalCalories(0.0)
                .totalProtein(0.0)
                .totalCarbs(0.0)
                .totalFats(0.0)
                .mealItems(new ArrayList<>())
                .build();

        // Add meal items
        if (request.getMealItems() != null) {
            for (MealItemRequest itemReq : request.getMealItems()) {
                MealItemModel item = MealItemModel.builder()
                        .meal(meal)
                        .itemName(itemReq.getItemName())
                        .calories(itemReq.getCalories())
                        .protein(itemReq.getProtein())
                        .carbs(itemReq.getCarbs())
                        .fats(itemReq.getFats())
                        .quantity(itemReq.getQuantity())
                        .build();
                meal.getMealItems().add(item);
            }
            recalculateTotals(meal);
        }

        MealModel saved = mealRepository.save(meal);
        return mapToResponse(saved);
    }

    public List<MealResponse> getMealsByUser(Long userId) {
        return mealRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MealResponse getMealById(Long mealId) {
        MealModel meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + mealId));
        return mapToResponse(meal);
    }

    @Transactional
    public MealResponse confirmMeal(Long mealId) {
        MealModel meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + mealId));

        if (meal.getStatus() == MealStatus.COMPLETED) {
            throw new IllegalStateException("Meal is already confirmed");
        }

        meal.setStatus(MealStatus.COMPLETED);
        MealModel saved = mealRepository.save(meal);

        // Update daily goal consumed nutrients
        if (saved.getDailyGoal() != null) {
            dailyGoalService.updateConsumedNutrients(saved.getDailyGoal().getId());
        }

        return mapToResponse(saved);
    }

    @Transactional
    public MealResponse updateMealItems(Long mealId, List<MealItemRequest> itemRequests) {
        MealModel meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + mealId));

        if (meal.getStatus() == MealStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update items of a confirmed meal");
        }

        // Clear existing items and add new ones
        meal.getMealItems().clear();
        for (MealItemRequest itemReq : itemRequests) {
            MealItemModel item = MealItemModel.builder()
                    .meal(meal)
                    .itemName(itemReq.getItemName())
                    .calories(itemReq.getCalories())
                    .protein(itemReq.getProtein())
                    .carbs(itemReq.getCarbs())
                    .fats(itemReq.getFats())
                    .quantity(itemReq.getQuantity())
                    .build();
            meal.getMealItems().add(item);
        }

        recalculateTotals(meal);
        MealModel saved = mealRepository.save(meal);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteMeal(Long mealId) {
        MealModel meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with id: " + mealId));

        if (meal.getStatus() == MealStatus.COMPLETED) {
            throw new IllegalStateException("Cannot delete a confirmed meal");
        }

        mealRepository.delete(meal);
    }

    // --- Helpers ---

    private void recalculateTotals(MealModel meal) {
        double totalCal = 0, totalPro = 0, totalCarb = 0, totalFat = 0;
        for (MealItemModel item : meal.getMealItems()) {
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;
            totalCal += (item.getCalories() != null ? item.getCalories() : 0) * qty;
            totalPro += (item.getProtein() != null ? item.getProtein() : 0) * qty;
            totalCarb += (item.getCarbs() != null ? item.getCarbs() : 0) * qty;
            totalFat += (item.getFats() != null ? item.getFats() : 0) * qty;
        }
        meal.setTotalCalories(totalCal);
        meal.setTotalProtein(totalPro);
        meal.setTotalCarbs(totalCarb);
        meal.setTotalFats(totalFat);
    }

    private MealResponse mapToResponse(MealModel meal) {
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
