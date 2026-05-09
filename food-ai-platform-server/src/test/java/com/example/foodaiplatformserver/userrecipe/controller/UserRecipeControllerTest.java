package com.example.foodaiplatformserver.userrecipe.controller;

import com.example.foodaiplatformserver.auth.repository.UserAccountRepository;
import com.example.foodaiplatformserver.user.repository.UserPreferenceRepository;
import com.example.foodaiplatformserver.userrecipe.repository.UserRecipeHistoryRepository;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class UserRecipeControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private UserRecipeHistoryRepository userRecipeHistoryRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        userRecipeHistoryRepository.deleteAll();
        userPreferenceRepository.deleteAll();
        userAccountRepository.deleteAll();
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @DisplayName("선택한 레시피 이력을 저장하고 최신순으로 limit 만큼 조회한다")
    @Test
    void savesAndReadsHistoriesWithLimitInLatestOrder() throws Exception {
        AuthSession user = signupAndLogin("user1@example.com");

        mockMvc.perform(post("/user-recipes")
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipe_id": 101,
                                  "recipe_name": "첫 번째 레시피",
                                  "category": "한식"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.history_id").isNumber())
                .andExpect(jsonPath("$.recipe_id").value(101))
                .andExpect(jsonPath("$.recipe_name").value("첫 번째 레시피"))
                .andExpect(jsonPath("$.category").value("한식"))
                .andExpect(jsonPath("$.selected_at").exists());

        mockMvc.perform(post("/user-recipes")
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipe_id": 102,
                                  "recipe_name": "두 번째 레시피",
                                  "category": "중식"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/user-recipes")
                        .header("Authorization", "Bearer " + user.accessToken())
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipe_id").value(102))
                .andExpect(jsonPath("$[0].recipe_name").value("두 번째 레시피"));
    }

    @DisplayName("다른 사용자의 레시피 이력은 조회되지 않는다")
    @Test
    void returnsOnlyCurrentUserHistories() throws Exception {
        AuthSession firstUser = signupAndLogin("user1@example.com");
        AuthSession secondUser = signupAndLogin("user2@example.com");

        createHistory(firstUser.accessToken(), 101, "첫 사용자 레시피", "한식");
        createHistory(secondUser.accessToken(), 201, "둘째 사용자 레시피", "양식");

        mockMvc.perform(get("/user-recipes")
                        .header("Authorization", "Bearer " + firstUser.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipe_id").value(101))
                .andExpect(jsonPath("$[0].recipe_name").value("첫 사용자 레시피"));
    }

    @DisplayName("limit가 1 미만이면 검증 에러를 반환한다")
    @Test
    void returnsValidationErrorForInvalidLimit() throws Exception {
        AuthSession user = signupAndLogin("user1@example.com");

        mockMvc.perform(get("/user-recipes")
                        .header("Authorization", "Bearer " + user.accessToken())
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @DisplayName("레시피 이름이나 카테고리가 너무 길면 검증 에러를 반환한다")
    @Test
    void returnsValidationErrorForTooLongFields() throws Exception {
        AuthSession user = signupAndLogin("user1@example.com");
        String longRecipeName = "가".repeat(256);
        String longCategory = "나".repeat(101);

        mockMvc.perform(post("/user-recipes")
                        .header("Authorization", "Bearer " + user.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipe_id": 101,
                                  "recipe_name": "%s",
                                  "category": "%s"
                                }
                                """.formatted(longRecipeName, longCategory)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details[*].field", hasItem("category")))
                .andExpect(jsonPath("$.details[*].field", hasItem("recipeName")));
    }

    private void createHistory(String accessToken, int recipeId, String recipeName, String category) throws Exception {
        mockMvc.perform(post("/user-recipes")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "recipe_id": %d,
                                  "recipe_name": "%s",
                                  "category": "%s"
                                }
                                """.formatted(recipeId, recipeName, category)))
                .andExpect(status().isCreated());
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
