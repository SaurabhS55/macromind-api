package com.macromind.macromind_api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.macromind.macromind_api.constants.Constants;
import com.macromind.macromind_api.dtos.MealItemRequest;
import com.macromind.macromind_api.dtos.MealRequest;
import com.macromind.macromind_api.dtos.MealResponse;
import com.macromind.macromind_api.models.UserModel;
import com.macromind.macromind_api.repositories.UserRepository;
import com.macromind.macromind_api.services.MealService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.API_PREFIX + "/meals")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<MealResponse> createMeal(
            Authentication authentication,
            @RequestBody MealRequest request) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(mealService.createMeal(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<MealResponse>> getAllMeals(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(mealService.getMealsByUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MealResponse> getMealById(@PathVariable Long id) {
        return ResponseEntity.ok(mealService.getMealById(id));
    }

    @PutMapping("/{id}/items")
    public ResponseEntity<MealResponse> updateMealItems(
            @PathVariable Long id,
            @RequestBody List<MealItemRequest> itemRequests) {
        return ResponseEntity.ok(mealService.updateMealItems(id, itemRequests));
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<MealResponse> confirmMeal(@PathVariable Long id) {
        return ResponseEntity.ok(mealService.confirmMeal(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication authentication) {
        UserModel user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();
        return user.getId();
    }
}
