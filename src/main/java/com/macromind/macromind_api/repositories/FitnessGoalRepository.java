package com.macromind.macromind_api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.macromind.macromind_api.enums.FitnessGoalStatus;
import com.macromind.macromind_api.models.FitnessGoalModel;

public interface FitnessGoalRepository extends JpaRepository<FitnessGoalModel, Long> {
    List<FitnessGoalModel> findByUserId(Long userId);

    Optional<FitnessGoalModel> findByUserIdAndStatus(Long userId, FitnessGoalStatus status);
}
