package com.example.foodaiplatformserver.recipe.service;

import com.example.foodaiplatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiplatformserver.fridgeitem.service.CurrentFridgeItemFinder;
import com.example.foodaiplatformserver.recipe.dto.RecipeRecommendationItemResponse;
import com.example.foodaiplatformserver.recipe.dto.RecipeRecommendationResponse;
import com.example.foodaiplatformserver.user.entity.UserPreference;
import com.example.foodaiplatformserver.user.repository.UserPreferenceRepository;
import com.example.foodaiplatformserver.user.service.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeRecommendationService {

    private final CurrentFridgeItemFinder currentFridgeItemFinder;
    private final CurrentUserProvider currentUserProvider;
    private final UserPreferenceRepository userPreferenceRepository;
    private final RecipeRecommendationEngine recipeRecommendationEngine;

    public RecipeRecommendationService(CurrentFridgeItemFinder currentFridgeItemFinder,
                                       CurrentUserProvider currentUserProvider,
                                       UserPreferenceRepository userPreferenceRepository,
                                       RecipeRecommendationEngine recipeRecommendationEngine) {
        this.currentFridgeItemFinder = currentFridgeItemFinder;
        this.currentUserProvider = currentUserProvider;
        this.userPreferenceRepository = userPreferenceRepository;
        this.recipeRecommendationEngine = recipeRecommendationEngine;
    }

    @Transactional(readOnly = true)
    public RecipeRecommendationResponse getRecommendations() {
        Long userId = currentUserProvider.getCurrentUserId();
        List<FridgeItem> fridgeItems = currentFridgeItemFinder.findCurrentUserItems();
        Optional<UserPreference> userPreference = userPreferenceRepository.findByUserId(userId);

        RecipeRecommendationResult result = recipeRecommendationEngine.recommend(
                new RecipeRecommendationCriteria(fridgeItems, userPreference)
        );

        return new RecipeRecommendationResponse(
                toResponse(result.availableNow()),
                toResponse(result.needFewIngredients())
        );
    }

    private List<RecipeRecommendationItemResponse> toResponse(List<RecommendedRecipe> recipes) {
        return recipes.stream()
                .map(recipe -> new RecipeRecommendationItemResponse(
                        recipe.recipeId(),
                        recipe.recipeName(),
                        recipe.category(),
                        recipe.expiringIngredientsUsed(),
                        recipe.allIngredients(),
                        recipe.missingIngredients(),
                        recipe.instructions()
                ))
                .toList();
    }
}
