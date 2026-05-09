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
            case "auto" -> recommendInAutoMode(criteria);
            default -> throw new IllegalArgumentException("Unsupported recommendation provider: " + provider);
        };
    }

    private RecipeRecommendationResult recommendInAutoMode(RecipeRecommendationCriteria criteria) {
        if (!hasGeminiApiKey()) {
            return mockRecipeRecommendationEngine.recommend(criteria);
        }

        try {
            return geminiRecipeRecommendationEngine.recommend(criteria);
        } catch (RuntimeException exception) {
            return mockRecipeRecommendationEngine.recommend(criteria);
        }
    }

    private boolean hasGeminiApiKey() {
        return geminiApiKey != null && !geminiApiKey.isBlank();
    }
}
