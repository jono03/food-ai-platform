package com.example.foodaiplatformserver.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecipeRecommendationResponse(
        @JsonProperty("available_now")
        List<RecipeRecommendationItemResponse> availableNow,

        @JsonProperty("need_few_ingredients")
        List<RecipeRecommendationItemResponse> needFewIngredients
) {
}
