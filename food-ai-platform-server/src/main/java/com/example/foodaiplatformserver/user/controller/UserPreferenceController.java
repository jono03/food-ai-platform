package com.example.foodaiplatformserver.user.controller;

import com.example.foodaiplatformserver.user.dto.UserPreferenceResponse;
import com.example.foodaiplatformserver.user.dto.UserPreferenceUpsertRequest;
import com.example.foodaiplatformserver.user.service.UserPreferenceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me/preferences")
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @PutMapping
    public UserPreferenceResponse upsert(@Valid @RequestBody UserPreferenceUpsertRequest request) {
        return userPreferenceService.upsert(request);
    }

    @GetMapping
    public UserPreferenceResponse getCurrentUserPreference() {
        return userPreferenceService.getCurrentUserPreference();
    }
}
