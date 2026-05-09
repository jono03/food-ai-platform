package com.example.foodaiplatformserver.recipe.controller;

import com.example.foodaiplatformserver.auth.repository.UserAccountRepository;
import com.example.foodaiplatformserver.fridgeitem.entity.FridgeItem;
import com.example.foodaiplatformserver.fridgeitem.entity.StorageLocation;
import com.example.foodaiplatformserver.fridgeitem.repository.FridgeItemRepository;
import com.example.foodaiplatformserver.user.repository.UserPreferenceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class RecipeRecommendationControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private FridgeItemRepository fridgeItemRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        fridgeItemRepository.deleteAll();
        userPreferenceRepository.deleteAll();
        userAccountRepository.deleteAll();
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @DisplayName("현재 식재료와 선호 정보를 기반으로 추천 결과를 분류해 반환한다")
    @Test
    void returnsRecommendationsUsingCurrentIngredientsAndPreference() throws Exception {
        AuthSession user = signupAndLogin("recommend@example.com");
        savePreference(user.accessToken(), """
                {
                  "favorite_cuisines": ["KOREAN"],
                  "difficulty_preference": "EASY",
                  "quick_meal_preferred": false
                }
                """);
        saveFridgeItem(user.userId(), "대파", "1단", 1);
        saveFridgeItem(user.userId(), "달걀", "4개", 0);
        saveFridgeItem(user.userId(), "양파", "1개", 5);
        saveFridgeItem(user.userId(), "우유", "500ml", 7);
        saveFridgeItem(user.userId(), "소고기", "300g", 2);
        saveFridgeItem(user.userId(), "다진 마늘", "100g", 14);

        mockMvc.perform(get("/recipes/recommendations")
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available_now", hasSize(1)))
                .andExpect(jsonPath("$.available_now[0].recipe_id").value(101))
                .andExpect(jsonPath("$.available_now[0].recipe_name").value("대파 달걀 볶음"))
                .andExpect(jsonPath("$.available_now[0].missing_ingredients", hasSize(0)))
                .andExpect(jsonPath("$.need_few_ingredients", hasSize(2)))
                .andExpect(jsonPath("$.need_few_ingredients[*].recipe_id", hasItem(102)));
    }

    @DisplayName("추천 가능한 레시피가 없으면 빈 배열을 반환한다")
    @Test
    void returnsEmptyListsWhenNoRecommendationExists() throws Exception {
        AuthSession user = signupAndLogin("empty@example.com");
        savePreference(user.accessToken(), """
                {
                  "favorite_cuisines": ["WESTERN"],
                  "difficulty_preference": "HARD",
                  "quick_meal_preferred": true
                }
                """);
        saveFridgeItem(user.userId(), "김치", "1포기", 10);

        mockMvc.perform(get("/recipes/recommendations")
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available_now", hasSize(0)))
                .andExpect(jsonPath("$.need_few_ingredients", hasSize(0)));
    }

    @DisplayName("유통기한 임박 재료가 없어도 만들 수 있는 레시피는 available_now로 추천된다")
    @Test
    void returnsAvailableNowWithoutExpiringIngredients() throws Exception {
        AuthSession user = signupAndLogin("nonexpiring@example.com");
        savePreference(user.accessToken(), """
                {
                  "favorite_cuisines": ["KOREAN"],
                  "difficulty_preference": "EASY",
                  "quick_meal_preferred": false
                }
                """);
        saveFridgeItem(user.userId(), "대파", "1단", 10);
        saveFridgeItem(user.userId(), "달걀", "4개", 10);
        saveFridgeItem(user.userId(), "양파", "1개", 10);
        saveFridgeItem(user.userId(), "우유", "500ml", 10);

        mockMvc.perform(get("/recipes/recommendations")
                        .header("Authorization", "Bearer " + user.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available_now", hasSize(1)))
                .andExpect(jsonPath("$.available_now[0].recipe_id").value(101))
                .andExpect(jsonPath("$.available_now[0].expiring_ingredients_used", hasSize(0)));
    }

    @DisplayName("토큰 없이 추천 API를 호출하면 인증 에러를 반환한다")
    @Test
    void returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/recipes/recommendations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private void savePreference(String accessToken, String body) throws Exception {
        mockMvc.perform(put("/users/me/preferences")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    private void saveFridgeItem(Long userId, String name, String quantity, int expirationOffsetDays) {
        fridgeItemRepository.save(new FridgeItem(
                userId,
                name,
                quantity,
                StorageLocation.REFRIGERATED,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(expirationOffsetDays)
        ));
    }

    private AuthSession signupAndLogin(String email) throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "password123!"
                                }
                                """.formatted(email, email)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "password123!"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        return new AuthSession(
                jsonNode.get("access_token").asText(),
                jsonNode.get("user").get("user_id").asLong()
        );
    }

    private record AuthSession(
            String accessToken,
            Long userId
    ) {
    }
}
