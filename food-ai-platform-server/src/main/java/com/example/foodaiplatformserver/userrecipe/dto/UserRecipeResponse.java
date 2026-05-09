package com.example.foodaiplatformserver.userrecipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record UserRecipeResponse(
        @JsonProperty("history_id")
        Long historyId,

        @JsonProperty("recipe_id")
        Long recipeId,

        @JsonProperty("recipe_name")
        String recipeName,

        @JsonProperty("category")
        String category,

        @JsonProperty("selected_at")
        LocalDateTime selectedAt
) {
}
