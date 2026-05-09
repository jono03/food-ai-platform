package com.example.foodaiplatformserver.recipe.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultRecipeRecommendationEngineTest {

    private final RecipeRecommendationResult geminiResult = new RecipeRecommendationResult(
            List.of(new RecommendedRecipe(1L, "gemini", "한식", List.of(), List.of(), List.of(), List.of())),
            List.of()
    );
    private final RecipeRecommendationResult mockResult = new RecipeRecommendationResult(
            List.of(new RecommendedRecipe(2L, "mock", "한식", List.of(), List.of(), List.of(), List.of())),
            List.of()
    );

    @DisplayName("auto 모드에서 API 키가 있으면 Gemini 엔진을 사용한다")
    @Test
    void usesGeminiWhenApiKeyExistsInAutoMode() {
        DefaultRecipeRecommendationEngine engine = new DefaultRecipeRecommendationEngine(
                criteria -> geminiResult,
                criteria -> mockResult,
                "auto",
                "secret"
        );

        RecipeRecommendationResult result = engine.recommend(emptyCriteria());

        assertThat(result).isEqualTo(geminiResult);
    }

    @DisplayName("auto 모드에서 API 키가 없으면 mock 엔진을 사용한다")
    @Test
    void usesMockWhenApiKeyMissingInAutoMode() {
        DefaultRecipeRecommendationEngine engine = new DefaultRecipeRecommendationEngine(
                criteria -> geminiResult,
                criteria -> mockResult,
                "auto",
                ""
        );

        RecipeRecommendationResult result = engine.recommend(emptyCriteria());

        assertThat(result).isEqualTo(mockResult);
    }

    @DisplayName("auto 모드에서 Gemini 엔진이 실패하면 mock 엔진으로 폴백한다")
    @Test
    void fallsBackToMockWhenGeminiFailsInAutoMode() {
        DefaultRecipeRecommendationEngine engine = new DefaultRecipeRecommendationEngine(
                criteria -> {
                    throw new IllegalStateException("Gemini failed");
                },
                criteria -> mockResult,
                "auto",
                "secret"
        );

        RecipeRecommendationResult result = engine.recommend(emptyCriteria());

        assertThat(result).isEqualTo(mockResult);
    }

    @DisplayName("gemini 모드에서는 Gemini 엔진 실패를 그대로 반환한다")
    @Test
    void throwsWhenGeminiFailsInGeminiMode() {
        DefaultRecipeRecommendationEngine engine = new DefaultRecipeRecommendationEngine(
                criteria -> {
                    throw new IllegalStateException("Gemini failed");
                },
                criteria -> mockResult,
                "gemini",
                "secret"
        );

        assertThatThrownBy(() -> engine.recommend(emptyCriteria()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Gemini failed");
    }

    private RecipeRecommendationCriteria emptyCriteria() {
        return new RecipeRecommendationCriteria(List.of(), Optional.empty());
    }
}
