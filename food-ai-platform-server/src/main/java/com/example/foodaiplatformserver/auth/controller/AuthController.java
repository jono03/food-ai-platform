package com.example.foodaiplatformserver.auth.controller;

import com.example.foodaiplatformserver.auth.dto.AuthLoginRequest;
import com.example.foodaiplatformserver.auth.dto.AuthLoginResponse;
import com.example.foodaiplatformserver.auth.dto.AuthSignupRequest;
import com.example.foodaiplatformserver.auth.dto.AuthSignupResponse;
import com.example.foodaiplatformserver.auth.dto.AuthUserResponse;
import com.example.foodaiplatformserver.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthSignupResponse signup(@Valid @RequestBody AuthSignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public AuthLoginResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthUserResponse me() {
        return authService.me();
    }
}
