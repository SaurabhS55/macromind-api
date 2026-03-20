package com.macromind.macromind_api.dtos;

import java.time.LocalDateTime;

import com.macromind.macromind_api.enums.ActivityLevel;
import com.macromind.macromind_api.enums.FitnessGoalStatus;
import com.macromind.macromind_api.enums.GoalType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FitnessGoalResponse {
    private Long id;
    private Long userId;
    private Double height;
    private Double weight;
    private Double goalWeight;
    private Double targetCalories;
    private Double targetProtein;
    private Double targetCarbs;
    private Double targetFats;
    private GoalType goalType;
    private ActivityLevel activityLevel;
    private FitnessGoalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
