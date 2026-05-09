package com.example.foodaiplatformserver.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class CorsIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @DisplayName("프론트 origin의 preflight 요청은 인증 없이 허용된다")
    @Test
    void allowsPreflightFromFrontendOrigin() throws Exception {
        MockMvc mockMvc = webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(options("/users/me/preferences")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5500")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5500"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }
}
