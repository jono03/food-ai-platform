package com.example.foodaiplatformserver.user.service;

import com.example.foodaiplatformserver.user.dto.UserPreferenceResponse;
import com.example.foodaiplatformserver.user.dto.UserPreferenceUpsertRequest;
import com.example.foodaiplatformserver.user.entity.DifficultyPreference;
import com.example.foodaiplatformserver.user.entity.FavoriteCuisine;
import com.example.foodaiplatformserver.user.entity.UserPreference;
import com.example.foodaiplatformserver.user.exception.UserPreferenceNotFoundException;
import com.example.foodaiplatformserver.user.repository.UserPreferenceRepository;
import com.example.foodaiplatformserver.common.support.KeyedLockExecutor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;
    private final CurrentUserProvider currentUserProvider;
    private final KeyedLockExecutor keyedLockExecutor;
    private final TransactionTemplate transactionTemplate;

    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository,
                                 CurrentUserProvider currentUserProvider,
                                 KeyedLockExecutor keyedLockExecutor,
                                 TransactionTemplate transactionTemplate) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.currentUserProvider = currentUserProvider;
        this.keyedLockExecutor = keyedLockExecutor;
        this.transactionTemplate = transactionTemplate;
    }

    public UserPreferenceResponse upsert(UserPreferenceUpsertRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        LocalDateTime updatedAt = LocalDateTime.now();
        List<FavoriteCuisine> favoriteCuisines = request.favoriteCuisines().stream()
                .map(FavoriteCuisine::valueOf)
                .toList();
        DifficultyPreference difficultyPreference = DifficultyPreference.valueOf(request.difficultyPreference());

        return keyedLockExecutor.execute("preference:" + userId, () ->
                upsertInternal(userId, favoriteCuisines, difficultyPreference, request.quickMealPreferred(), updatedAt)
        );
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

    private UserPreferenceResponse upsertInternal(Long userId,
                                                  List<FavoriteCuisine> favoriteCuisines,
                                                  DifficultyPreference difficultyPreference,
                                                  boolean quickMealPreferred,
                                                  LocalDateTime updatedAt) {
        return userPreferenceRepository.findByUserId(userId)
                .map(existing -> saveUpdated(userId, favoriteCuisines, difficultyPreference, quickMealPreferred, updatedAt))
                .orElseGet(() -> createOrRecover(userId, favoriteCuisines, difficultyPreference, quickMealPreferred, updatedAt));
    }

    private UserPreferenceResponse saveUpdated(Long userId,
                                               List<FavoriteCuisine> favoriteCuisines,
                                               DifficultyPreference difficultyPreference,
                                               boolean quickMealPreferred,
                                               LocalDateTime updatedAt) {
        return transactionTemplate.execute(status -> {
            UserPreference existing = userPreferenceRepository.findByUserId(userId)
                    .orElseThrow(() -> new UserPreferenceNotFoundException(userId));
            existing.update(favoriteCuisines, difficultyPreference, quickMealPreferred, updatedAt);
            return toResponse(userPreferenceRepository.save(existing));
        });
    }

    private UserPreferenceResponse createOrRecover(Long userId,
                                                   List<FavoriteCuisine> favoriteCuisines,
                                                   DifficultyPreference difficultyPreference,
                                                   boolean quickMealPreferred,
                                                   LocalDateTime updatedAt) {
        try {
            return transactionTemplate.execute(status -> {
                UserPreference created = new UserPreference(
                        userId,
                        favoriteCuisines,
                        difficultyPreference,
                        quickMealPreferred,
                        updatedAt
                );
                return toResponse(userPreferenceRepository.saveAndFlush(created));
            });
        } catch (DataIntegrityViolationException exception) {
            return saveUpdated(userId, favoriteCuisines, difficultyPreference, quickMealPreferred, updatedAt);
        }
    }
}
