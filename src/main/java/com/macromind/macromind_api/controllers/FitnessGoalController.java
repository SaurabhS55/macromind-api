package com.macromind.macromind_api.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.macromind.macromind_api.constants.Constants;
import com.macromind.macromind_api.models.FitnessGoalModel;
import com.macromind.macromind_api.services.FitnessGoalService;

@RestController
@RequestMapping(Constants.API_PREFIX + "/fitness-goal")
public class FitnessGoalController {
    
    @Autowired
    private FitnessGoalService fitnessGoalService;

    @GetMapping("/goals")
    public ResponseEntity<List<FitnessGoalModel>> getAllFitnessGoals() {
        return ResponseEntity.ok(fitnessGoalService.getAllFitnessGoals());
    }
    
}
