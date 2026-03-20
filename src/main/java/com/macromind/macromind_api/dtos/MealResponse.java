package com.macromind.macromind_api.dtos;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import com.macromind.macromind_api.enums.MealStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealResponse {
    private Long id;
    private Long userId;
    private Long dailyGoalId;
    private Date mealDate;
    private MealStatus status;
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFats;
    private List<MealItemResponse> mealItems;
    private LocalDateTime createdAt;
    private LocalDateTime consumedAt;
}
