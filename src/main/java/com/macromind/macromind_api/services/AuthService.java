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

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final WebClient.Builder webClientBuilder;

    @Value("${springdoc.swagger-ui.oauth.client-id}")
    private String googleClientId;

    @Value("${springdoc.swagger-ui.oauth.client-secret}")
    private String googleClientSecret;

    @Value("${google.oauth2.redirect-uri}")
    private String googleRedirectUri;

    @Value("${google.oauth2.base-url}")
    private String googleOAuth2BaseUrl;

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

    public AuthResponse googleLogin(String code) {
        try {
            // 1. Exchange authorization code for tokens
            Map<String, Object> tokenResponse = webClientBuilder.build()
                    .post()
                    .uri(googleOAuth2BaseUrl + "/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters
                            .fromFormData("code", code)
                            .with("client_id", googleClientId)
                            .with("client_secret", googleClientSecret)
                            .with("redirect_uri", googleRedirectUri)
                            .with("grant_type", "authorization_code"))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (tokenResponse == null || !tokenResponse.containsKey("id_token")) {
                throw new IllegalStateException("Failed to retrieve ID token from Google");
            }

            // 2. Extract user info by using id_token
            String userInfoUrl = googleOAuth2BaseUrl + "/tokeninfo?id_token=" + tokenResponse.get("id_token");

            
            Map<String, Object> userInfo = webClientBuilder.build()
                    .get()
                    .uri(userInfoUrl)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            String email = (String) userInfo.get("email");
            String name = (String) userInfo.get("name");

            // 3. Find existing user or create new one
            Optional<UserModel> existingUser = repository.findByEmail(email);
            UserModel user;

            if (existingUser.isPresent()) {
                user = existingUser.get();
                log.info("Existing Google user logged in: {}", email);
            } else {
                // Generate random password seeded from email
                String randomPassword = generateRandomPassword(email);
                user = UserModel.builder()
                        .name(name != null ? name : email)
                        .email(email)
                        .password(passwordEncoder.encode(randomPassword))
                        .build();
                repository.save(user);
                log.info("New Google user created: {}", email);
            }

            // 4. Generate JWT and return
            String jwtToken = jwtUtil.generateToken(userDetailsService.loadUserByUsername(user.getEmail()));
            return AuthResponse.builder()
                    .token(jwtToken)
                    .build();

        } catch (Exception e) {
            log.error("Error during Google login: {}", e.getMessage());
            throw new IllegalStateException("Google login failed: " + e.getMessage());
        }
    }

    private String generateRandomPassword(String email) {
        Random random = new Random(email.hashCode());
        StringBuilder sb = new StringBuilder(32);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        for (int i = 0; i < 32; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
