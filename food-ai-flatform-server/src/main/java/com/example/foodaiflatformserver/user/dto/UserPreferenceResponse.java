package com.example.foodaiflatformserver.user.dto;

import com.example.foodaiflatformserver.user.entity.UserPreference;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Set;

public record UserPreferenceResponse(
        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("favorite_cuisines")
        Set<String> favoriteCuisines,

        @JsonProperty("difficulty_preference")
        String difficultyPreference,

        @JsonProperty("quick_meal_preferred")
        boolean quickMealPreferred,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {

    public static UserPreferenceResponse from(UserPreference preference) {
        return new UserPreferenceResponse(
                preference.getUser().getId(),
                preference.getFavoriteCuisines().stream().map(Enum::name).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)),
                preference.getDifficultyPreference().name(),
                preference.isQuickMealPreferred(),
                preference.getUpdatedAt()
        );
    }
}
