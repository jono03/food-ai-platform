package com.example.foodaiplatformserver.auth.controller;

import com.example.foodaiplatformserver.auth.dto.AuthLoginRequest;
import com.example.foodaiplatformserver.auth.dto.AuthLoginResponse;
import com.example.foodaiplatformserver.auth.dto.AuthSignupRequest;
import com.example.foodaiplatformserver.auth.dto.AuthSignupResponse;
import com.example.foodaiplatformserver.auth.dto.AuthUserResponse;
import com.example.foodaiplatformserver.auth.service.AuthService;
import com.example.foodaiplatformserver.common.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증", description = "회원가입, 로그인, 내 정보 조회 API")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입", description = "이메일과 비밀번호로 회원가입합니다.")
    public AuthSignupResponse signup(@Valid @RequestBody AuthSignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인 후 액세스 토큰과 사용자 정보를 반환합니다.")
    public AuthLoginResponse login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 인증된 사용자 정보를 조회합니다.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
    public AuthUserResponse me() {
        return authService.me();
    }
}
