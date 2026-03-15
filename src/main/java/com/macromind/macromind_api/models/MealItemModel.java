package com.macromind.macromind_api.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "meal_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealItemModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private MealModel meal;

    private String itemName;
    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fats;
    private Integer quantity;
}
