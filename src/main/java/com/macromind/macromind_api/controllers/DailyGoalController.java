package com.macromind.macromind_api.controllers;

import java.sql.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.macromind.macromind_api.constants.Constants;
import com.macromind.macromind_api.dtos.DailyGoalHeatmapResponse;
import com.macromind.macromind_api.dtos.DailyGoalRequest;
import com.macromind.macromind_api.dtos.DailyGoalResponse;
import com.macromind.macromind_api.models.UserModel;
import com.macromind.macromind_api.repositories.UserRepository;
import com.macromind.macromind_api.services.DailyGoalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.API_PREFIX + "/daily-goal")
@RequiredArgsConstructor
public class DailyGoalController {

    private final DailyGoalService dailyGoalService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<DailyGoalResponse> createDailyGoal(
            Authentication authentication,
            @RequestBody DailyGoalRequest request) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(dailyGoalService.createOrGetDailyGoal(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<DailyGoalResponse>> getAllDailyGoals(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(dailyGoalService.getDailyGoalsByUser(userId));
    }

    @GetMapping("/today")
    public ResponseEntity<DailyGoalResponse> getTodayGoal(Authentication authentication) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(dailyGoalService.getTodayGoal(userId));
    }

    @GetMapping("/{date}")
    public ResponseEntity<DailyGoalResponse> getDailyGoalByDate(
            Authentication authentication,
            @PathVariable Date date) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(dailyGoalService.getDailyGoal(userId, date));
    }

    @GetMapping("/heatmap/{year}")
    public ResponseEntity<List<DailyGoalHeatmapResponse>> getYearlyHeatmap(
            Authentication authentication,
            @PathVariable int year) {
        Long userId = getUserId(authentication);
        return ResponseEntity.ok(dailyGoalService.getYearlyHeatmap(userId, year));
    }

    private Long getUserId(Authentication authentication) {
        UserModel user = userRepository.findByEmail(authentication.getName())
                .orElseThrow();
        return user.getId();
    }
}
