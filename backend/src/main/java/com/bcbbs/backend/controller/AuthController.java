package com.bcbbs.backend.controller;

import com.bcbbs.backend.dto.*;
import com.bcbbs.backend.entity.User;
import com.bcbbs.backend.security.JwtService;
import com.bcbbs.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        User user = (User) authentication.getPrincipal();
        String token = jwtService.generateToken(user);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Username already exists"));
        }
        
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Email already exists"));
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .role(User.Role.USER)
                .enabled(true)
                .build();
        
        user = userService.save(user);
        String token = jwtService.generateToken(user);
        
        AuthResponse authResponse = AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Registration successful", authResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> getCurrentUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        AuthResponse authResponse = AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().name())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }
}

