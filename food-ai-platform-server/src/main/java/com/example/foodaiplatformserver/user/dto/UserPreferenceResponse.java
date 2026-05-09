package com.example.foodaiplatformserver.user.dto;

import com.example.foodaiplatformserver.user.entity.DifficultyPreference;
import com.example.foodaiplatformserver.user.entity.FavoriteCuisine;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.LocalDateTime;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserPreferenceResponse(
        @JsonProperty("user_id")
        Long userId,
        @JsonProperty("favorite_cuisines")
        List<FavoriteCuisine> favoriteCuisines,
        @JsonProperty("difficulty_preference")
        DifficultyPreference difficultyPreference,
        @JsonProperty("quick_meal_preferred")
        boolean quickMealPreferred,
        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
}
