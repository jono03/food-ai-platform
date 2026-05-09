package com.example.foodaiplatformserver.user.dto;

import com.example.foodaiplatformserver.user.entity.DifficultyPreference;
import com.example.foodaiplatformserver.user.entity.FavoriteCuisine;
import com.example.foodaiplatformserver.user.validation.EnumValue;
import com.example.foodaiplatformserver.user.validation.EnumValues;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserPreferenceUpsertRequest(
        @JsonProperty("favorite_cuisines")
        @NotEmpty(message = "선호 카테고리는 최소 1개 이상 선택해야 합니다.")
        @EnumValues(enumClass = FavoriteCuisine.class, message = "선호 카테고리 값이 올바르지 않습니다.")
        List<String> favoriteCuisines,

        @JsonProperty("difficulty_preference")
        @NotNull(message = "난이도 선호는 필수입니다.")
        @EnumValue(enumClass = DifficultyPreference.class, message = "난이도 선호 값이 올바르지 않습니다.")
        String difficultyPreference,

        @JsonProperty("quick_meal_preferred")
        @NotNull(message = "간편식 선호 여부는 필수입니다.")
        Boolean quickMealPreferred
) {
}
