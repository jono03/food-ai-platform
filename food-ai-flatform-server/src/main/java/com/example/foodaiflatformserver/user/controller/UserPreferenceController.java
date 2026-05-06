package com.example.foodaiflatformserver.user.controller;

import com.example.foodaiflatformserver.auth.security.UserPrincipal;
import com.example.foodaiflatformserver.user.dto.UserPreferenceResponse;
import com.example.foodaiflatformserver.user.dto.UserPreferenceSaveRequest;
import com.example.foodaiflatformserver.user.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @PutMapping
    public UserPreferenceResponse save(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UserPreferenceSaveRequest request
    ) {
        return userPreferenceService.save(principal, request);
    }

    @GetMapping
    public UserPreferenceResponse get(@AuthenticationPrincipal UserPrincipal principal) {
        return userPreferenceService.get(principal);
    }
}
