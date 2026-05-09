package com.example.foodaiplatformserver.auth.controller;

import com.example.foodaiplatformserver.auth.repository.UserAccountRepository;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class AuthControllerTest {

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

    @DisplayName("회원가입 후 로그인하면 JWT를 발급하고 내 정보를 조회할 수 있다")
    @Test
    void signupLoginAndReadMe() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "홍길동",
                                  "email": "user@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."))
                .andExpect(jsonPath("$.user.username").value("홍길동"))
                .andExpect(jsonPath("$.user.email").value("user@example.com"));

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(loginResponse);
        Long userId = jsonNode.get("user").get("user_id").asLong();
        String accessToken = jsonNode.get("access_token").asText();

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(userId))
                .andExpect(jsonPath("$.username").value("홍길동"))
                .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @DisplayName("중복 이메일 회원가입은 충돌 에러를 반환한다")
    @Test
    void returnsConflictForDuplicateEmail() throws Exception {
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

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "김영희",
                                  "email": "user@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @DisplayName("잘못된 로그인 정보는 인증 에러를 반환한다")
    @Test
    void returnsUnauthorizedForInvalidLogin() throws Exception {
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

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @DisplayName("회원가입 요청의 문자열 길이가 컬럼 상한을 넘으면 검증 에러를 반환한다")
    @Test
    void returnsBadRequestForTooLongSignupFields() throws Exception {
        String longUsername = "가".repeat(101);
        String longEmailLocal = "a".repeat(244);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s@example.com",
                                  "password": "password123!"
                                }
                                """.formatted(longUsername, longEmailLocal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.details[*].field", hasItem("email")))
                .andExpect(jsonPath("$.details[*].field", hasItem("username")));
    }
}
