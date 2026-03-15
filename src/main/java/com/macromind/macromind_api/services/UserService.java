package com.macromind.macromind_api.services;

import org.springframework.stereotype.Service;

import com.macromind.macromind_api.dtos.UserResponse;
import com.macromind.macromind_api.exceptions.ResourceNotFoundException;
import com.macromind.macromind_api.models.UserModel;
import com.macromind.macromind_api.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(String email) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    private UserResponse mapToResponse(UserModel user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .dob(user.getDob())
                .gender(user.getGender())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
