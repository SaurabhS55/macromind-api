package com.macromind.macromind_api.models;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.macromind.macromind_api.enums.MealStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "meals")
public class MealModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserModel user;

    @ManyToOne
    private DailyGoalModel dailyGoal;

    @OneToMany(mappedBy = "meal")
    private List<Mealitem> mealItems;
    
    private Date mealDate;

    @Enumerated(EnumType.STRING)
    private MealStatus status;
    
    private Double totalCalories;
    private Double totalProtein;
    private Double totalCarbs;
    private Double totalFats;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime consumedAt;
}
