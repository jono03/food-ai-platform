package com.example.foodaiplatformserver.user.controller;

import com.example.foodaiplatformserver.user.dto.UserPreferenceResponse;
import com.example.foodaiplatformserver.user.dto.UserPreferenceUpsertRequest;
import com.example.foodaiplatformserver.user.service.UserPreferenceService;
import com.example.foodaiplatformserver.common.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/preferences")
@Tag(name = "사용자 선호도", description = "현재 사용자 선호도 조회 및 저장 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @PutMapping
    @Operation(summary = "선호도 저장", description = "현재 사용자 선호도를 저장하거나 갱신합니다.")
    public UserPreferenceResponse upsert(@Valid @RequestBody UserPreferenceUpsertRequest request) {
        return userPreferenceService.upsert(request);
    }

    @GetMapping
    @Operation(summary = "선호도 조회", description = "현재 사용자 선호도를 조회합니다.")
    public UserPreferenceResponse getCurrentUserPreference() {
        return userPreferenceService.getCurrentUserPreference();
    }
}
