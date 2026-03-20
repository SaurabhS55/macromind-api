package com.macromind.macromind_api.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.macromind.macromind_api.constants.Constants;
import com.macromind.macromind_api.dtos.FitnessGoalRequest;
import com.macromind.macromind_api.dtos.FitnessGoalResponse;
import com.macromind.macromind_api.models.UserModel;
import com.macromind.macromind_api.repositories.UserRepository;
import com.macromind.macromind_api.services.FitnessGoalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.API_PREFIX + "/fitness-goal")
@RequiredArgsConstructor
public class FitnessGoalController {

    private final FitnessGoalService fitnessGoalService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<FitnessGoalResponse> createGoal(
            Authentication authentication,
            @RequestBody FitnessGoalRequest request) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(fitnessGoalService.createGoal(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<FitnessGoalResponse>> getAllGoals(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(fitnessGoalService.getGoalsByUser(userId));
    }

    @GetMapping("/active")
    public ResponseEntity<FitnessGoalResponse> getActiveGoal(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(fitnessGoalService.getActiveGoal(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FitnessGoalResponse> updateGoal(
            @PathVariable Long id,
            @RequestBody FitnessGoalRequest request) {
        return ResponseEntity.ok(fitnessGoalService.updateGoal(id, request));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateGoal(@PathVariable Long id) {
        fitnessGoalService.deactivateGoal(id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserId(Authentication authentication) {
        UserModel user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();
        return user.getId();
    }
}
