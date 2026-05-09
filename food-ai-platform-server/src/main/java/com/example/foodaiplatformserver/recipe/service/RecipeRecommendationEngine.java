package com.example.foodaiplatformserver.recipe.service;

public interface RecipeRecommendationEngine {

    RecipeRecommendationResult recommend(RecipeRecommendationCriteria criteria);
}
