package com.macromind.macromind_api.dtos;

import java.sql.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealRequest {
    private Long dailyGoalId;
    private Date mealDate;
    private List<MealItemRequest> mealItems;
}
