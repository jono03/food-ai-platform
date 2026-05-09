package com.example.foodaiplatformserver.recipe.service;

import com.example.foodaiplatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiplatformserver.user.entity.UserPreference;

import java.util.List;
import java.util.Optional;

public record RecipeRecommendationCriteria(
        List<FridgeItem> fridgeItems,
        Optional<UserPreference> userPreference
) {
}
