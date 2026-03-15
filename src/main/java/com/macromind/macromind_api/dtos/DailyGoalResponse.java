package com.macromind.macromind_api.dtos;

import java.sql.Date;
import java.util.List;

import com.macromind.macromind_api.enums.DailyGoalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyGoalResponse {
    private Long id;
    private Long userId;
    private Date date;
    private Double targetCalories;
    private Double targetProtein;
    private Double targetCarbs;
    private Double targetFats;
    private Double consumedCalories;
    private Double consumedProtein;
    private Double consumedCarbs;
    private Double consumedFats;
    private DailyGoalStatus status;
    private List<MealResponse> meals;
}
