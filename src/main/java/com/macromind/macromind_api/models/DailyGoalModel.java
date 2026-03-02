package com.macromind.macromind_api.models;

import java.sql.Date;
import java.util.List;

import com.macromind.macromind_api.enums.DailyGoalStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "daily_goals", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "date"})
})
public class DailyGoalModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private UserModel user;

    @OneToMany(mappedBy = "dailyGoal")
    private List<MealModel> meals;

    private Date date;
    private Double targetCalories;
    private Double targetProtein;
    private Double targetCarbs;
    private Double targetFats;
    private Double consumedCalories;
    private Double consumedProtein;
    private Double consumedCarbs;
    private Double consumedFats;

    @Enumerated(EnumType.STRING)
    private DailyGoalStatus status;
}
