package com.example.foodaiflatformserver.user.service;

import com.example.foodaiflatformserver.common.support.KeyedLockExecutor;
import com.example.foodaiflatformserver.user.dto.UserPreferenceResponse;
import com.example.foodaiflatformserver.user.dto.UserPreferenceUpsertRequest;
import com.example.foodaiflatformserver.user.entity.DifficultyPreference;
import com.example.foodaiflatformserver.user.entity.FavoriteCuisine;
import com.example.foodaiflatformserver.user.entity.UserPreference;
import com.example.foodaiflatformserver.user.repository.UserPreferenceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserPreferenceServiceTest {

    private final UserPreferenceRepository userPreferenceRepository = mock(UserPreferenceRepository.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
    private final UserPreferenceService userPreferenceService = new UserPreferenceService(
            userPreferenceRepository,
            currentUserProvider,
            new KeyedLockExecutor(),
            new TransactionTemplate(transactionManager)
    );

    @DisplayName("최초 취향 저장 중 유니크 제약 충돌이 나면 기존 데이터를 다시 조회해 갱신한다")
    @Test
    void recoversFromDuplicatePreferenceInsert() {
        Long userId = 1L;
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        UserPreferenceUpsertRequest request = new UserPreferenceUpsertRequest(
                List.of("WESTERN"),
                "HARD",
                false
        );
        UserPreference existing = new UserPreference(
                userId,
                List.of(FavoriteCuisine.KOREAN),
                DifficultyPreference.EASY,
                true,
                LocalDateTime.now().minusDays(1)
        );

        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        when(currentUserProvider.getCurrentUserId()).thenReturn(userId);
        when(userPreferenceRepository.findByUserId(userId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(userPreferenceRepository.saveAndFlush(any(UserPreference.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate user_id"));
        when(userPreferenceRepository.save(existing)).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferenceResponse response = userPreferenceService.upsert(request);

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.favoriteCuisines()).containsExactly(FavoriteCuisine.WESTERN);
        assertThat(response.difficultyPreference()).isEqualTo(DifficultyPreference.HARD);
        assertThat(response.quickMealPreferred()).isFalse();
    }
}
