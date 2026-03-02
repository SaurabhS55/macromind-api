package com.macromind.macromind_api.repositories;

import com.macromind.macromind_api.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);
}
