package com.example.foodaiplatformserver.recipe.service;

import java.util.List;

public record RecommendedRecipe(
        Long recipeId,
        String recipeName,
        String category,
        List<String> expiringIngredientsUsed,
        List<String> allIngredients,
        List<String> missingIngredients,
        List<String> instructions
) {
}
