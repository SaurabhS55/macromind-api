package com.macromind.macromind_api.services;

import com.macromind.macromind_api.dtos.AuthRequest;
import com.macromind.macromind_api.dtos.AuthResponse;
import com.macromind.macromind_api.dtos.RegisterRequest;
import com.macromind.macromind_api.models.UserModel;
import com.macromind.macromind_api.repositories.AuthRepository;
import com.macromind.macromind_api.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthResponse register(RegisterRequest request) {
        var user = UserModel.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .dob(request.getDob())
                .gender(request.getGender())
                .build();
        repository.save(user);
        var jwtToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));
        var user = repository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}
