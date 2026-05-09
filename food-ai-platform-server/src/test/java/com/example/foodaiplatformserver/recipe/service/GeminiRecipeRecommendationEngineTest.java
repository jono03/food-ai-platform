package com.example.foodaiplatformserver.recipe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiRecipeRecommendationEngineTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final GeminiRecipeRecommendationEngine engine = new GeminiRecipeRecommendationEngine(
            objectMapper,
            null,
            "secret",
            "gemini-3.1-flash-lite",
            "gemini-2.5-flash-lite",
            "https://generativelanguage.googleapis.com/v1beta"
    );

    @DisplayName("스펙을 만족하는 Gemini 응답은 정상 파싱한다")
    @Test
    void parsesValidResponse() {
        RecipeRecommendationResult result = engine.parseRecommendationResult(validGeminiResponse("""
                {
                  "available_now": [
                    {
                      "recipe_id": 101,
                      "recipe_name": "대파 달걀 볶음",
                      "category": "한식",
                      "expiring_ingredients_used": ["대파"],
                      "all_ingredients": ["대파", "달걀"],
                      "missing_ingredients": [],
                      "instructions": ["1", "2", "3"]
                    }
                  ],
                  "need_few_ingredients": [
                    {
                      "recipe_id": 102,
                      "recipe_name": "소고기 장조림",
                      "category": "한식",
                      "expiring_ingredients_used": ["대파"],
                      "all_ingredients": ["소고기", "대파", "간장"],
                      "missing_ingredients": ["간장"],
                      "instructions": ["1", "2", "3"]
                    }
                  ]
                }
                """));

        assertThat(result.availableNow()).hasSize(1);
        assertThat(result.needFewIngredients()).hasSize(1);
    }

    @DisplayName("available_now에 missing_ingredients가 있으면 예외를 던진다")
    @Test
    void rejectsAvailableNowWithMissingIngredients() {
        assertThatThrownBy(() -> engine.parseRecommendationResult(validGeminiResponse("""
                {
                  "available_now": [
                    {
                      "recipe_id": 101,
                      "recipe_name": "대파 달걀 볶음",
                      "category": "한식",
                      "expiring_ingredients_used": ["대파"],
                      "all_ingredients": ["대파", "달걀", "우유"],
                      "missing_ingredients": ["우유"],
                      "instructions": ["1", "2", "3"]
                    }
                  ],
                  "need_few_ingredients": []
                }
                """)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("available_now");
    }

    @DisplayName("중복 recipe_id가 있으면 예외를 던진다")
    @Test
    void rejectsDuplicateRecipeIds() {
        assertThatThrownBy(() -> engine.parseRecommendationResult(validGeminiResponse("""
                {
                  "available_now": [
                    {
                      "recipe_id": 101,
                      "recipe_name": "대파 달걀 볶음",
                      "category": "한식",
                      "expiring_ingredients_used": ["대파"],
                      "all_ingredients": ["대파", "달걀"],
                      "missing_ingredients": [],
                      "instructions": ["1", "2", "3"]
                    }
                  ],
                  "need_few_ingredients": [
                    {
                      "recipe_id": 101,
                      "recipe_name": "소고기 장조림",
                      "category": "한식",
                      "expiring_ingredients_used": ["대파"],
                      "all_ingredients": ["소고기", "대파", "간장"],
                      "missing_ingredients": ["간장"],
                      "instructions": ["1", "2", "3"]
                    }
                  ]
                }
                """)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("duplicate");
    }

    @DisplayName("스펙 외 필드가 있으면 예외를 던진다")
    @Test
    void rejectsUnexpectedFields() {
        assertThatThrownBy(() -> engine.parseRecommendationResult(validGeminiResponse("""
                {
                  "available_now": [
                    {
                      "recipe_id": 101,
                      "recipe_name": "대파 달걀 볶음",
                      "category": "한식",
                      "expiring_ingredients_used": ["대파"],
                      "all_ingredients": ["대파", "달걀"],
                      "missing_ingredients": [],
                      "instructions": ["1", "2", "3"],
                      "extra": "nope"
                    }
                  ],
                  "need_few_ingredients": []
                }
                """)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fields");
    }

    private String validGeminiResponse(String innerJson) {
        return """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": %s
                          }
                        ]
                      }
                    }
                  ]
                }
                """.formatted(toJsonString(innerJson));
    }

    private String toJsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
