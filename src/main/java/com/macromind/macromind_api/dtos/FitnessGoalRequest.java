package com.macromind.macromind_api.dtos;

import com.macromind.macromind_api.enums.ActivityLevel;
import com.macromind.macromind_api.enums.GoalType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FitnessGoalRequest {
    private Double height;
    private Double weight;
    private Double goalWeight;
    private GoalType goalType;
    private ActivityLevel activityLevel;
}
