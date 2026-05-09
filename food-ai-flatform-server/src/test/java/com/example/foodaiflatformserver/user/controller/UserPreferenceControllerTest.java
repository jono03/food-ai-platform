package com.example.foodaiflatformserver.user.controller;

import com.example.foodaiflatformserver.user.entity.User;
import com.example.foodaiflatformserver.user.repository.UserPreferenceRepository;
import com.example.foodaiflatformserver.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        userPreferenceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("사용자 취향을 저장한 뒤 즉시 조회할 수 있다")
    @Test
    void saveAndGetPreferences() throws Exception {
        String accessToken = signupAndLogin("user@example.com");

        mockMvc.perform(put("/api/v1/users/me/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "favorite_cuisines": ["KOREAN", "JAPANESE"],
                                  "difficulty_preference": "EASY",
                                  "quick_meal_preferred": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").isNumber())
                .andExpect(jsonPath("$.favorite_cuisines[0]").value("KOREAN"))
                .andExpect(jsonPath("$.favorite_cuisines[1]").value("JAPANESE"))
                .andExpect(jsonPath("$.difficulty_preference").value("EASY"))
                .andExpect(jsonPath("$.quick_meal_preferred").value(true));

        mockMvc.perform(get("/api/v1/users/me/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite_cuisines[0]").value("KOREAN"))
                .andExpect(jsonPath("$.favorite_cuisines[1]").value("JAPANESE"))
                .andExpect(jsonPath("$.difficulty_preference").value("EASY"))
                .andExpect(jsonPath("$.quick_meal_preferred").value(true));
    }

    @DisplayName("동일 사용자가 다시 저장하면 기존 취향이 갱신된다")
    @Test
    void upsertsPreferences() throws Exception {
        String accessToken = signupAndLogin("user@example.com");

        mockMvc.perform(put("/api/v1/users/me/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "favorite_cuisines": ["KOREAN"],
                                  "difficulty_preference": "EASY",
                                  "quick_meal_preferred": true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/users/me/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "favorite_cuisines": ["WESTERN", "CHINESE"],
                                  "difficulty_preference": "HARD",
                                  "quick_meal_preferred": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite_cuisines[0]").value("WESTERN"))
                .andExpect(jsonPath("$.favorite_cuisines[1]").value("CHINESE"))
                .andExpect(jsonPath("$.difficulty_preference").value("HARD"))
                .andExpect(jsonPath("$.quick_meal_preferred").value(false));

        mockMvc.perform(get("/api/v1/users/me/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite_cuisines[0]").value("WESTERN"))
                .andExpect(jsonPath("$.favorite_cuisines[1]").value("CHINESE"))
                .andExpect(jsonPath("$.difficulty_preference").value("HARD"))
                .andExpect(jsonPath("$.quick_meal_preferred").value(false));
    }

    @DisplayName("잘못된 enum 값은 400 INVALID_REQUEST를 반환한다")
    @Test
    void rejectsInvalidEnumValues() throws Exception {
        String accessToken = signupAndLogin("user@example.com");

        mockMvc.perform(put("/api/v1/users/me/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "favorite_cuisines": ["KOREAN", "MEXICAN"],
                                  "difficulty_preference": "VERY_EASY",
                                  "quick_meal_preferred": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private String signupAndLogin(String email) throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "username": "홍길동",
                          "email": "%s",
                          "password": "Password123!"
                        }
                        """.formatted(email)));

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(email)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        return jsonNode.get("access_token").asText();
    }
}
