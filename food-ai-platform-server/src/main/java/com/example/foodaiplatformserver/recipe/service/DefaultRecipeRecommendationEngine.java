package com.example.foodaiplatformserver.recipe.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class DefaultRecipeRecommendationEngine implements RecipeRecommendationEngine {

    private final RecipeRecommendationEngine geminiRecipeRecommendationEngine;
    private final RecipeRecommendationEngine mockRecipeRecommendationEngine;
    private final String provider;
    private final String geminiApiKey;

    public DefaultRecipeRecommendationEngine(
            @Qualifier("geminiRecipeRecommendationEngine")
            RecipeRecommendationEngine geminiRecipeRecommendationEngine,
            @Qualifier("mockRecipeRecommendationEngine")
            RecipeRecommendationEngine mockRecipeRecommendationEngine,
            @Value("${app.recipe.recommendation.provider:auto}") String provider,
            @Value("${app.gemini.api-key:}") String geminiApiKey
    ) {
        this.geminiRecipeRecommendationEngine = geminiRecipeRecommendationEngine;
        this.mockRecipeRecommendationEngine = mockRecipeRecommendationEngine;
        this.provider = provider;
        this.geminiApiKey = geminiApiKey;
    }

    @Override
    public RecipeRecommendationResult recommend(RecipeRecommendationCriteria criteria) {
        return switch (provider.toLowerCase()) {
            case "gemini" -> geminiRecipeRecommendationEngine.recommend(criteria);
            case "mock" -> mockRecipeRecommendationEngine.recommend(criteria);
            case "auto" -> hasGeminiApiKey()
                    ? geminiRecipeRecommendationEngine.recommend(criteria)
                    : mockRecipeRecommendationEngine.recommend(criteria);
            default -> throw new IllegalArgumentException("Unsupported recommendation provider: " + provider);
        };
    }

    private boolean hasGeminiApiKey() {
        return geminiApiKey != null && !geminiApiKey.isBlank();
    }
}
