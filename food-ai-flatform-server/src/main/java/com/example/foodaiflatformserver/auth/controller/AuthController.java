package com.example.foodaiflatformserver.auth.controller;

import com.example.foodaiflatformserver.auth.dto.AuthUserResponse;
import com.example.foodaiflatformserver.auth.dto.LoginRequest;
import com.example.foodaiflatformserver.auth.dto.LoginResponse;
import com.example.foodaiflatformserver.auth.dto.SignupRequest;
import com.example.foodaiflatformserver.auth.dto.SignupResponse;
import com.example.foodaiflatformserver.auth.security.UserPrincipal;
import com.example.foodaiflatformserver.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthUserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return authService.getMe(principal);
    }
}
