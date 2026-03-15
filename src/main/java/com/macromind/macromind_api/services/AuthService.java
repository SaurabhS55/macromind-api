package com.macromind.macromind_api.services;

import com.macromind.macromind_api.dtos.AuthRequest;
import com.macromind.macromind_api.dtos.AuthResponse;
import com.macromind.macromind_api.dtos.CommonResponse;
import com.macromind.macromind_api.dtos.RegisterRequest;
import com.macromind.macromind_api.models.UserModel;
import com.macromind.macromind_api.repositories.AuthRepository;
import com.macromind.macromind_api.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public CommonResponse register(RegisterRequest request) {
        try {
                if(repository.findByEmail(request.getEmail()).isPresent()) {
                throw new Exception("User already exists");
            }
            var user = UserModel.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .dob(request.getDob())
                    .gender(request.getGender())
                    .build();
            repository.save(user);
            log.info("User registered successfully: {}", user.getEmail());
            return CommonResponse.builder()
                    .status(HttpStatus.OK.toString())
                    .message("User registered successfully")
                    .build();
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage());
            return CommonResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.toString())
                    .message(e.getMessage())
                    .build();
        }
    }

    public AuthResponse login(AuthRequest request) {
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));
            UserModel user = repository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            String jwtToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
            log.info("User logged in successfully: {}", user.getEmail());
            return AuthResponse.builder()
                    .token(jwtToken)
                    .build();
        }catch(Exception e){
            log.error("Error logging in: {}", e.getMessage());
            throw new IllegalStateException(e.getMessage());
        }
    }
}
