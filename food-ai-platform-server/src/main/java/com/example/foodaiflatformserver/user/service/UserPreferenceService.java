package com.example.foodaiflatformserver.user.service;

import com.example.foodaiflatformserver.user.dto.UserPreferenceResponse;
import com.example.foodaiflatformserver.user.dto.UserPreferenceUpsertRequest;
import com.example.foodaiflatformserver.user.entity.DifficultyPreference;
import com.example.foodaiflatformserver.user.entity.FavoriteCuisine;
import com.example.foodaiflatformserver.user.entity.UserPreference;
import com.example.foodaiflatformserver.user.exception.UserPreferenceNotFoundException;
import com.example.foodaiflatformserver.user.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final CurrentUserProvider currentUserProvider;

    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository, CurrentUserProvider currentUserProvider) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public UserPreferenceResponse upsert(UserPreferenceUpsertRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDateTime updatedAt = LocalDateTime.now();
        List<FavoriteCuisine> favoriteCuisines = request.favoriteCuisines().stream()
                .map(FavoriteCuisine::valueOf)
                .toList();
        DifficultyPreference difficultyPreference = DifficultyPreference.valueOf(request.difficultyPreference());

        UserPreference userPreference = userPreferenceRepository.findByUserId(userId)
                .map(existing -> {
                    existing.update(favoriteCuisines, difficultyPreference, request.quickMealPreferred(), updatedAt);
                    return existing;
                })
                .orElseGet(() -> new UserPreference(
                        userId,
                        favoriteCuisines,
                        difficultyPreference,
                        request.quickMealPreferred(),
                        updatedAt
                ));

        UserPreference saved = userPreferenceRepository.save(userPreference);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserPreferenceResponse getCurrentUserPreference() {
        Long userId = currentUserProvider.getCurrentUserId();

        return userPreferenceRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new UserPreferenceNotFoundException(userId));
    }

    private UserPreferenceResponse toResponse(UserPreference userPreference) {
        return new UserPreferenceResponse(
                userPreference.getUserId(),
                userPreference.getFavoriteCuisines(),
                userPreference.getDifficultyPreference(),
                userPreference.isQuickMealPreferred(),
                userPreference.getUpdatedAt()
        );
    }
}
