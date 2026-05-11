package com.hnust.easyweibo.backend.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hnust.easyweibo.backend.domain.dto.auth.LoginRequest;
import com.hnust.easyweibo.backend.domain.dto.auth.LoginResponse;
import com.hnust.easyweibo.backend.domain.dto.auth.RegisterRequest;
import com.hnust.easyweibo.backend.domain.dto.auth.ResetPasswordRequest;
import com.hnust.easyweibo.backend.domain.dto.user.UserResponse;
import com.hnust.easyweibo.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/reset-password")
    public java.util.Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return java.util.Map.of("message", "密码已重置，请使用新密码登录");
    }

    @GetMapping("/me")
    public UserResponse currentUser(@RequestHeader("Authorization") String authorization) {
        return authService.currentUser(authorization);
    }
}
