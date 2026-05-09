package com.example.foodaiplatformserver.recipe.service;

import java.util.List;

public record RecipeRecommendationResult(
        List<RecommendedRecipe> availableNow,
        List<RecommendedRecipe> needFewIngredients
) {
}
