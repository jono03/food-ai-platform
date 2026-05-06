package com.example.foodaiflatformserver.user.dto;

import com.example.foodaiflatformserver.user.entity.DifficultyPreference;
import com.example.foodaiflatformserver.user.entity.FavoriteCuisine;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UserPreferenceSaveRequest(
        @JsonProperty("favorite_cuisines")
        @NotEmpty(message = "선호 카테고리는 1개 이상 선택해야 합니다.")
        Set<FavoriteCuisine> favoriteCuisines,

        @JsonProperty("difficulty_preference")
        @NotNull(message = "선호 난이도는 필수입니다.")
        DifficultyPreference difficultyPreference,

        @JsonProperty("quick_meal_preferred")
        @NotNull(message = "간편식 선호 여부는 필수입니다.")
        Boolean quickMealPreferred
) {
}
