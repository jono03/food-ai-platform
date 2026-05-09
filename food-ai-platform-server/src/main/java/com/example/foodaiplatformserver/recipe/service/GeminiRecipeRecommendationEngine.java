package com.example.foodaiplatformserver.recipe.service;

import com.example.foodaiplatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiplatformserver.user.entity.UserPreference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component("geminiRecipeRecommendationEngine")
public class GeminiRecipeRecommendationEngine implements RecipeRecommendationEngine {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String model;
    private final String fallbackModel;
    private final String apiUrl;

    @Autowired
    public GeminiRecipeRecommendationEngine(ObjectMapper objectMapper,
                                            @Value("${app.gemini.api-key:}") String apiKey,
                                            @Value("${app.gemini.model:gemini-2.5-flash-lite}") String model,
                                            @Value("${app.gemini.fallback-model:gemini-2.5-flash-lite}") String fallbackModel,
                                            @Value("${app.gemini.api-url:https://generativelanguage.googleapis.com/v1beta}") String apiUrl) {
        this(objectMapper, HttpClient.newHttpClient(), apiKey, model, fallbackModel, apiUrl);
    }

    GeminiRecipeRecommendationEngine(ObjectMapper objectMapper,
                                     HttpClient httpClient,
                                     String apiKey,
                                     String model,
                                     String fallbackModel,
                                     String apiUrl) {
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.model = model;
        this.fallbackModel = fallbackModel;
        this.apiUrl = apiUrl;
    }

    @Override
    public RecipeRecommendationResult recommend(RecipeRecommendationCriteria criteria) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is missing.");
        }

        String responseBody = callGemini(model, buildRequestBody(criteria));
        return parseRecommendationResult(responseBody);
    }

    private String callGemini(String targetModel, String requestBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + "/models/" + targetModel + ":generateContent"))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 400) {
                if (response.statusCode() == 404 && shouldFallback(targetModel)) {
                    return callGemini(fallbackModel, requestBody);
                }
                throw new IllegalStateException("Gemini API request failed with status " + response.statusCode());
            }
            return response.body();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini API request interrupted.", exception);
        }
    }

    private boolean shouldFallback(String targetModel) {
        return fallbackModel != null
                && !fallbackModel.isBlank()
                && !fallbackModel.equals(targetModel);
    }

    private String buildRequestBody(RecipeRecommendationCriteria criteria) {
        try {
            return objectMapper.writeValueAsString(new GeminiGenerateContentRequest(
                    List.of(new GeminiContent(List.of(new GeminiPart(buildPrompt(criteria))))),
                    new GeminiGenerationConfig(
                            "application/json",
                            RecommendationSchemaFactory.responseSchema()
                    )
            ));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private String buildPrompt(RecipeRecommendationCriteria criteria) {
        String fridgeItems = criteria.fridgeItems().isEmpty()
                ? "- 없음"
                : criteria.fridgeItems().stream()
                .map(this::formatFridgeItem)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("- 없음");

        String preference = formatPreference(criteria.userPreference());

        return """
                너는 스마트 냉장고 서비스의 레시피 추천 API 백엔드 엔진이다.
                너의 출력은 다른 시스템이 그대로 JSON 파싱한다.
                반드시 아래 요구사항을 모두 지켜라.

                절대 규칙:
                1. JSON 객체 하나만 반환한다.
                2. 마크다운 코드블록(```), 설명 문장, 주석, 서문, 후문을 절대 출력하지 마라.
                3. 최상위 필드는 정확히 "available_now", "need_few_ingredients" 두 개만 사용한다.
                4. 두 필드의 값은 항상 배열이어야 한다.
                5. 각 레시피 객체의 필드는 정확히 아래 7개만 사용한다.
                   - recipe_id
                   - recipe_name
                   - category
                   - expiring_ingredients_used
                   - all_ingredients
                   - missing_ingredients
                   - instructions
                6. 모든 필드명은 snake_case를 사용한다.
                7. expiring_ingredients_used, all_ingredients, missing_ingredients, instructions는 항상 문자열 배열이어야 한다.
                8. 추천 결과가 없으면 null, 생략, 빈 문자열 대신 반드시 빈 배열 []을 반환한다.
                9. 스펙에 없는 필드는 절대 추가하지 마라.
                10. category는 반드시 한국어 문자열 예: "한식", "중식", "일식", "양식" 으로 반환한다.
                11. recipe_id는 반드시 숫자로 반환한다.
                12. recipe_name은 비어 있으면 안 된다.
                13. instructions는 최소 3개 이상의 조리 단계 문자열을 넣는다. 정말 불가능한 경우에도 빈 배열이 아니라 가능한 단계 배열을 구성한다.
                14. expiring_ingredients_used와 missing_ingredients의 원소는 반드시 all_ingredients 안에 포함되어야 한다.
                15. available_now의 각 레시피는 missing_ingredients가 정확히 [] 이어야 한다.

                분류 규칙:
                - available_now: 현재 보유 식재료만으로 만들 수 있는 레시피만 넣는다. missing_ingredients는 반드시 [] 여야 한다.
                - need_few_ingredients: 1개 또는 2개의 재료만 추가 구매하면 만들 수 있는 레시피만 넣는다.
                - 3개 이상 재료가 부족한 레시피는 추천하지 마라.
                - 두 배열에 같은 레시피를 중복으로 넣지 마라.
                - 한 배열 내부에서도 같은 recipe_id를 중복으로 넣지 마라.
                - 현재 식재료와 사용자 취향을 최대한 반영하라.
                - 유통기한 임박 재료를 우선 활용하라.
                - expiring_ingredients_used에는 실제로 그 레시피에 사용되며, 현재 식재료 중 유통기한이 임박한 재료만 넣어라.
                - all_ingredients에는 레시피 전체 재료를 넣어라.
                - missing_ingredients에는 현재 없는 재료만 넣어라.
                - all_ingredients, missing_ingredients, expiring_ingredients_used에는 수량, 괄호 설명, 추가 문장을 붙이지 말고 재료명만 넣어라.

                응답 형식 예시:
                {
                  "available_now": [
                    {
                      "recipe_id": 101,
                      "recipe_name": "예시 레시피",
                      "category": "한식",
                      "expiring_ingredients_used": ["대파"],
                      "all_ingredients": ["대파", "달걀"],
                      "missing_ingredients": [],
                      "instructions": ["1단계", "2단계", "3단계"]
                    }
                  ],
                  "need_few_ingredients": []
                }

                현재 식재료:
                %s

                사용자 취향:
                %s
                """.formatted(fridgeItems, preference);
    }

    private String formatFridgeItem(FridgeItem fridgeItem) {
        return "- 이름: %s, 수량: %s, 보관: %s, 유통기한: %s".formatted(
                fridgeItem.getName(),
                fridgeItem.getQuantity(),
                fridgeItem.getStorageLocation().name(),
                fridgeItem.getExpirationDate()
        );
    }

    private String formatPreference(Optional<UserPreference> userPreference) {
        if (userPreference.isEmpty()) {
            return "- 없음";
        }

        UserPreference preference = userPreference.get();
        return """
                - 선호 cuisines: %s
                - 난이도: %s
                - 간편식 선호: %s
                """.formatted(
                preference.getFavoriteCuisines(),
                preference.getDifficultyPreference(),
                preference.isQuickMealPreferred()
        );
    }

    private RecipeRecommendationResult parseRecommendationResult(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new IllegalStateException("Gemini response did not contain recommendation JSON.");
            }

            JsonNode json = objectMapper.readTree(textNode.asText());
            return new RecipeRecommendationResult(
                    parseRecipes(json.path("available_now")),
                    parseRecipes(json.path("need_few_ingredients"))
            );
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private List<RecommendedRecipe> parseRecipes(JsonNode itemsNode) {
        if (!itemsNode.isArray()) {
            return List.of();
        }

        return java.util.stream.StreamSupport.stream(itemsNode.spliterator(), false)
                .map(this::parseRecipe)
                .toList();
    }

    private RecommendedRecipe parseRecipe(JsonNode node) {
        return new RecommendedRecipe(
                node.path("recipe_id").asLong(),
                node.path("recipe_name").asText(""),
                node.path("category").asText(""),
                toStringList(node.path("expiring_ingredients_used")),
                toStringList(node.path("all_ingredients")),
                toStringList(node.path("missing_ingredients")),
                toStringList(node.path("instructions"))
        );
    }

    private List<String> toStringList(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }

        return java.util.stream.StreamSupport.stream(node.spliterator(), false)
                .map(JsonNode::asText)
                .toList();
    }

    private record GeminiGenerateContentRequest(
            List<GeminiContent> contents,
            @JsonProperty("generationConfig")
            GeminiGenerationConfig generationConfig
    ) {
    }

    private record GeminiContent(
            List<GeminiPart> parts
    ) {
    }

    private record GeminiPart(
            String text
    ) {
    }

    private record GeminiGenerationConfig(
            @JsonProperty("response_mime_type")
            String responseMimeType,
            @JsonProperty("response_schema")
            JsonNode responseJsonSchema
    ) {
    }
}
