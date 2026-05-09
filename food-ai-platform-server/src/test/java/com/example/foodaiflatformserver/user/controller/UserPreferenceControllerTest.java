package com.example.foodaiflatformserver.user.controller;

import com.example.foodaiflatformserver.auth.repository.UserAccountRepository;
import com.example.foodaiflatformserver.user.repository.UserPreferenceRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class UserPreferenceControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        userPreferenceRepository.deleteAll();
        userAccountRepository.deleteAll();
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @DisplayName("선호 정보를 저장한 뒤 즉시 조회할 수 있다")
    @Test
    void savesAndReadsPreference() throws Exception {
        AuthSession authSession = signupAndLogin();

        mockMvc.perform(put("/users/me/preferences")
                        .header("Authorization", "Bearer " + authSession.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "favorite_cuisines": ["KOREAN", "JAPANESE"],
                                  "difficulty_preference": "EASY",
                                  "quick_meal_preferred": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(authSession.userId()))
                .andExpect(jsonPath("$.favorite_cuisines", hasSize(2)))
                .andExpect(jsonPath("$.favorite_cuisines[0]").value("KOREAN"))
                .andExpect(jsonPath("$.favorite_cuisines[1]").value("JAPANESE"))
                .andExpect(jsonPath("$.difficulty_preference").value("EASY"))
                .andExpect(jsonPath("$.quick_meal_preferred").value(true))
                .andExpect(jsonPath("$.updated_at").exists());

        mockMvc.perform(get("/users/me/preferences")
                        .header("Authorization", "Bearer " + authSession.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(authSession.userId()))
                .andExpect(jsonPath("$.favorite_cuisines", hasSize(2)))
                .andExpect(jsonPath("$.favorite_cuisines[0]").value("KOREAN"))
                .andExpect(jsonPath("$.favorite_cuisines[1]").value("JAPANESE"))
                .andExpect(jsonPath("$.difficulty_preference").value("EASY"))
                .andExpect(jsonPath("$.quick_meal_preferred").value(true))
                .andExpect(jsonPath("$.updated_at").exists());
    }

    @DisplayName("같은 사용자가 다시 저장하면 기존 선호 정보가 갱신된다")
    @Test
    void upsertsExistingPreference() throws Exception {
        AuthSession authSession = signupAndLogin();

        mockMvc.perform(put("/users/me/preferences")
                        .header("Authorization", "Bearer " + authSession.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "favorite_cuisines": ["KOREAN", "JAPANESE"],
                                  "difficulty_preference": "EASY",
                                  "quick_meal_preferred": true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/me/preferences")
                        .header("Authorization", "Bearer " + authSession.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "favorite_cuisines": ["WESTERN"],
                                  "difficulty_preference": "HARD",
                                  "quick_meal_preferred": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite_cuisines", hasSize(1)))
                .andExpect(jsonPath("$.favorite_cuisines[0]").value("WESTERN"))
                .andExpect(jsonPath("$.difficulty_preference").value("HARD"))
                .andExpect(jsonPath("$.quick_meal_preferred").value(false));

        mockMvc.perform(get("/users/me/preferences")
                        .header("Authorization", "Bearer " + authSession.accessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite_cuisines", hasSize(1)))
                .andExpect(jsonPath("$.favorite_cuisines[0]").value("WESTERN"))
                .andExpect(jsonPath("$.difficulty_preference").value("HARD"))
                .andExpect(jsonPath("$.quick_meal_preferred").value(false));
    }

    @DisplayName("잘못된 enum 값은 validation 에러를 반환한다")
    @Test
    void returnsValidationErrorForInvalidEnum() throws Exception {
        AuthSession authSession = signupAndLogin();

        mockMvc.perform(put("/users/me/preferences")
                        .header("Authorization", "Bearer " + authSession.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "favorite_cuisines": ["KOREAN", "THAI"],
                                  "difficulty_preference": "VERY_EASY",
                                  "quick_meal_preferred": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details", hasSize(2)))
                .andExpect(jsonPath("$.details[0].field").value("difficultyPreference"))
                .andExpect(jsonPath("$.details[1].field").value("favoriteCuisines"));
    }

    @DisplayName("토큰 없이 선호 조회를 호출하면 인증 에러를 반환한다")
    @Test
    void returnsUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/users/me/preferences"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private AuthSession signupAndLogin() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "홍길동",
                                  "email": "user@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!"
                                }
                                """))
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
