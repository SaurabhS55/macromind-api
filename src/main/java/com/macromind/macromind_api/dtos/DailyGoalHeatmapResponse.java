package com.macromind.macromind_api.dtos;

import java.sql.Date;

import com.macromind.macromind_api.enums.DailyGoalStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyGoalHeatmapResponse {
    private Date date;
    private DailyGoalStatus status;
    private Double caloriePercentage;
    private Double proteinPercentage;
    private Double carbsPercentage;
    private Double fatsPercentage;
}
