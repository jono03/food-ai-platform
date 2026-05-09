package com.example.foodaiplatformserver.userrecipe.service;

import com.example.foodaiplatformserver.user.service.CurrentUserProvider;
import com.example.foodaiplatformserver.userrecipe.dto.UserRecipeCreateRequest;
import com.example.foodaiplatformserver.userrecipe.dto.UserRecipeResponse;
import com.example.foodaiplatformserver.userrecipe.entity.UserRecipeHistory;
import com.example.foodaiplatformserver.userrecipe.repository.UserRecipeHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserRecipeService {

    private static final int DEFAULT_LIMIT = 20;

    private final UserRecipeHistoryRepository userRecipeHistoryRepository;
    private final CurrentUserProvider currentUserProvider;

    public UserRecipeService(UserRecipeHistoryRepository userRecipeHistoryRepository,
                             CurrentUserProvider currentUserProvider) {
        this.userRecipeHistoryRepository = userRecipeHistoryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public UserRecipeResponse create(UserRecipeCreateRequest request) {
        Long userId = currentUserProvider.getCurrentUserId();
        UserRecipeHistory history = new UserRecipeHistory(
                userId,
                request.recipeId(),
                request.recipeName(),
                request.category(),
                LocalDateTime.now()
        );

        return toResponse(userRecipeHistoryRepository.save(history));
    }

    @Transactional(readOnly = true)
    public List<UserRecipeResponse> getMyHistories(Integer limit) {
        Long userId = currentUserProvider.getCurrentUserId();
        int resolvedLimit = limit == null ? DEFAULT_LIMIT : limit;

        return userRecipeHistoryRepository.findByUserIdOrderBySelectedAtDescIdDesc(
                        userId,
                        PageRequest.of(0, resolvedLimit)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserRecipeResponse toResponse(UserRecipeHistory history) {
        return new UserRecipeResponse(
                history.getId(),
                history.getRecipeId(),
                history.getRecipeName(),
                history.getCategory(),
                history.getSelectedAt()
        );
    }
}
