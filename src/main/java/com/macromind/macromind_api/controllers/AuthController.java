package com.macromind.macromind_api.controllers;

import com.macromind.macromind_api.dtos.AuthRequest;
import com.macromind.macromind_api.dtos.AuthResponse;
import com.macromind.macromind_api.dtos.CommonResponse;
import com.macromind.macromind_api.dtos.RegisterRequest;
import com.macromind.macromind_api.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService service;

    @Value("${frontend.url}")
    private String frontendUrl;

    @PostMapping("/register")
    public ResponseEntity<CommonResponse> register(
            @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody AuthRequest request) {
        return ResponseEntity.ok(service.login(request));
    }

    @GetMapping("/google/callback")
    public ResponseEntity<Void> googleLogin(@RequestParam("code") String code) {
        AuthResponse response = service.googleLogin(code);
        String redirectUrl = frontendUrl + "?token=" + response.getToken();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
    
}
