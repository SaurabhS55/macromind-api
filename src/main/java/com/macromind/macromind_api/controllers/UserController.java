package com.macromind.macromind_api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.macromind.macromind_api.constants.Constants;
import com.macromind.macromind_api.dtos.UserResponse;
import com.macromind.macromind_api.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.API_PREFIX + "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfile(authentication.getName()));
    }
}
