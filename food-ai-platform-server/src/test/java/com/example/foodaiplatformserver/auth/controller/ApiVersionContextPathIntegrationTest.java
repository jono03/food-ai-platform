package com.example.foodaiplatformserver.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "server.servlet.context-path=/api/v1")
class ApiVersionContextPathIntegrationTest {

    private final MockMvc mockMvc;

    ApiVersionContextPathIntegrationTest(WebApplicationContext webApplicationContext) {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @DisplayName("회원가입 API는 /api/v1 base path 아래에서 동작한다")
    @Test
    void signupWorksUnderApiVersionContextPath() throws Exception {
        mockMvc.perform(post("/api/v1/auth/signup")
                        .contextPath("/api/v1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "홍길동",
                                  "email": "versioned@example.com",
                                  "password": "password123!"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("versioned@example.com"));
    }

    @DisplayName("보호된 API는 /api/v1 base path 아래에서 인증을 요구한다")
    @Test
    void protectedApiRequiresAuthUnderApiVersionContextPath() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/preferences")
                        .contextPath("/api/v1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }
}
