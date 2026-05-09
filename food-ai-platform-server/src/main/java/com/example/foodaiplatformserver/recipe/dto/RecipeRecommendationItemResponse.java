package com.example.foodaiplatformserver.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecipeRecommendationItemResponse(
        @JsonProperty("recipe_id")
        Long recipeId,

        @JsonProperty("recipe_name")
        String recipeName,

        @JsonProperty("category")
        String category,

        @JsonProperty("expiring_ingredients_used")
        List<String> expiringIngredientsUsed,

        @JsonProperty("all_ingredients")
        List<String> allIngredients,

        @JsonProperty("missing_ingredients")
        List<String> missingIngredients,

        @JsonProperty("instructions")
        List<String> instructions
) {
}
