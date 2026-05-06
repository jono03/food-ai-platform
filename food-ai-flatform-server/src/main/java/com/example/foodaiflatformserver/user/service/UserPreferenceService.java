package com.example.foodaiflatformserver.user.service;

import com.example.foodaiflatformserver.auth.security.UserPrincipal;
import com.example.foodaiflatformserver.common.exception.NotFoundException;
import com.example.foodaiflatformserver.user.dto.UserPreferenceResponse;
import com.example.foodaiflatformserver.user.dto.UserPreferenceSaveRequest;
import com.example.foodaiflatformserver.user.entity.User;
import com.example.foodaiflatformserver.user.entity.UserPreference;
import com.example.foodaiflatformserver.user.repository.UserPreferenceRepository;
import com.example.foodaiflatformserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @Transactional
    public UserPreferenceResponse save(UserPrincipal principal, UserPreferenceSaveRequest request) {
        User user = userRepository.findById(principal.id())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        UserPreference preference = userPreferenceRepository.findByUserId(user.getId())
                .map(existing -> {
                    existing.update(
                            request.favoriteCuisines(),
                            request.difficultyPreference(),
                            request.quickMealPreferred()
                    );
                    return existing;
                })
                .orElseGet(() -> {
                    UserPreference created = UserPreference.builder()
                            .favoriteCuisines(request.favoriteCuisines())
                            .difficultyPreference(request.difficultyPreference())
                            .quickMealPreferred(request.quickMealPreferred())
                            .build();
                    user.assignPreference(created);
                    return created;
                });

        UserPreference savedPreference = userPreferenceRepository.save(preference);
        return UserPreferenceResponse.from(savedPreference);
    }

    public UserPreferenceResponse get(UserPrincipal principal) {
        UserPreference preference = userPreferenceRepository.findByUserId(principal.id())
                .orElseThrow(() -> new NotFoundException("사용자 취향 정보를 찾을 수 없습니다."));

        return UserPreferenceResponse.from(preference);
    }
}
