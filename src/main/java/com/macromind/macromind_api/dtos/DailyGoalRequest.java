package com.macromind.macromind_api.dtos;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyGoalRequest {
    private Date date;
    private Double consumedCalories;
    private Double consumedProtein;
    private Double consumedCarbs;
    private Double consumedFats;
}
