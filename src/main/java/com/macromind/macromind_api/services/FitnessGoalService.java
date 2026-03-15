package com.macromind.macromind_api.services;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.macromind.macromind_api.dtos.FitnessGoalRequest;
import com.macromind.macromind_api.dtos.FitnessGoalResponse;
import com.macromind.macromind_api.enums.FitnessGoalStatus;
import com.macromind.macromind_api.enums.Gender;
import com.macromind.macromind_api.exceptions.ResourceNotFoundException;
import com.macromind.macromind_api.models.FitnessGoalModel;
import com.macromind.macromind_api.models.UserModel;
import com.macromind.macromind_api.repositories.FitnessGoalRepository;
import com.macromind.macromind_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FitnessGoalService {

    private final FitnessGoalRepository fitnessGoalRepository;
    private final UserRepository userRepository;

    public FitnessGoalResponse createGoal(Long userId, FitnessGoalRequest request) {
        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Deactivate any existing active goal
        fitnessGoalRepository.findByUserIdAndStatus(userId, FitnessGoalStatus.ACTIVE)
                .ifPresent(existingGoal -> {
                    existingGoal.setStatus(FitnessGoalStatus.INACTIVE);
                    fitnessGoalRepository.save(existingGoal);
                });

        // Calculate macros from user profile + request
        int age = calculateAge(user);
        double targetCalories = calculateTargetCalories(
                request.getWeight(), request.getHeight(), age,
                user.getGender(), request.getActivityLevel(), request.getGoalType());
        double targetProtein = (targetCalories * 0.30) / 4.0; // 30% of calories, 4 cal/g
        double targetCarbs = (targetCalories * 0.40) / 4.0; // 40% of calories, 4 cal/g
        double targetFats = (targetCalories * 0.30) / 9.0; // 30% of calories, 9 cal/g

        FitnessGoalModel goal = FitnessGoalModel.builder()
                .user(user)
                .height(request.getHeight())
                .weight(request.getWeight())
                .goalWeight(request.getGoalWeight())
                .goalType(request.getGoalType())
                .activityLevel(request.getActivityLevel())
                .targetCalories(Math.round(targetCalories * 100.0) / 100.0)
                .targetProtein(Math.round(targetProtein * 100.0) / 100.0)
                .targetCarbs(Math.round(targetCarbs * 100.0) / 100.0)
                .targetFats(Math.round(targetFats * 100.0) / 100.0)
                .status(FitnessGoalStatus.ACTIVE)
                .build();

        FitnessGoalModel saved = fitnessGoalRepository.save(goal);
        return mapToResponse(saved);
    }

    public List<FitnessGoalResponse> getGoalsByUser(Long userId) {
        return fitnessGoalRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FitnessGoalResponse getActiveGoal(Long userId) {
        FitnessGoalModel goal = fitnessGoalRepository.findByUserIdAndStatus(userId, FitnessGoalStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active fitness goal found for user: " + userId));
        return mapToResponse(goal);
    }

    public FitnessGoalResponse updateGoal(Long goalId, FitnessGoalRequest request) {
        FitnessGoalModel goal = fitnessGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with id: " + goalId));

        UserModel user = goal.getUser();
        int age = calculateAge(user);

        goal.setHeight(request.getHeight());
        goal.setWeight(request.getWeight());
        goal.setGoalWeight(request.getGoalWeight());
        goal.setGoalType(request.getGoalType());
        goal.setActivityLevel(request.getActivityLevel());

        // Recalculate macros
        double targetCalories = calculateTargetCalories(
                request.getWeight(), request.getHeight(), age,
                user.getGender(), request.getActivityLevel(), request.getGoalType());
        goal.setTargetCalories(Math.round(targetCalories * 100.0) / 100.0);
        goal.setTargetProtein(Math.round((targetCalories * 0.30 / 4.0) * 100.0) / 100.0);
        goal.setTargetCarbs(Math.round((targetCalories * 0.40 / 4.0) * 100.0) / 100.0);
        goal.setTargetFats(Math.round((targetCalories * 0.30 / 9.0) * 100.0) / 100.0);

        FitnessGoalModel saved = fitnessGoalRepository.save(goal);
        return mapToResponse(saved);
    }

    public void deactivateGoal(Long goalId) {
        FitnessGoalModel goal = fitnessGoalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with id: " + goalId));
        goal.setStatus(FitnessGoalStatus.INACTIVE);
        fitnessGoalRepository.save(goal);
    }

    // --- Macro Calculation (Mifflin-St Jeor) ---

    private double calculateTargetCalories(Double weightKg, Double heightCm, int age,
            Gender gender, com.macromind.macromind_api.enums.ActivityLevel activityLevel,
            com.macromind.macromind_api.enums.GoalType goalType) {
        // BMR using Mifflin-St Jeor equation
        double bmr;
        if (gender == Gender.MALE) {
            bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5;
        } else {
            bmr = (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161;
        }

        // TDEE = BMR × activity multiplier
        double activityMultiplier = switch (activityLevel) {
            case LOW -> 1.375;
            case MEDIUM -> 1.55;
            case HIGH -> 1.725;
        };
        double tdee = bmr * activityMultiplier;

        // Goal adjustment
        return switch (goalType) {
            case CUT -> tdee - 500;
            case BULK -> tdee + 500;
            case MAINTAIN -> tdee;
        };
    }

    private int calculateAge(UserModel user) {
        if (user.getDob() == null)
            return 25; // default age if not set
        LocalDate birthDate = user.getDob().toLocalDate();
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private FitnessGoalResponse mapToResponse(FitnessGoalModel goal) {
        return FitnessGoalResponse.builder()
                .id(goal.getId())
                .userId(goal.getUser().getId())
                .height(goal.getHeight())
                .weight(goal.getWeight())
                .goalWeight(goal.getGoalWeight())
                .targetCalories(goal.getTargetCalories())
                .targetProtein(goal.getTargetProtein())
                .targetCarbs(goal.getTargetCarbs())
                .targetFats(goal.getTargetFats())
                .goalType(goal.getGoalType())
                .activityLevel(goal.getActivityLevel())
                .status(goal.getStatus())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}
