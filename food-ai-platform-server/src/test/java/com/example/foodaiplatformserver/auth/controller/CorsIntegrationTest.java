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

    private static final String PREFERENCES_ENDPOINT = "/users/me/preferences";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @DisplayName("프론트 origin의 preflight 요청은 인증 없이 허용된다")
    @Test
    void allowsPreflightFromFrontendOrigin() throws Exception {
        assertPreflightAllowed("http://localhost:5500");
    }

    @DisplayName("127.0.0.1 origin의 preflight 요청은 인증 없이 허용된다")
    @Test
    void allowsPreflightFromLoopbackOrigin() throws Exception {
        assertPreflightAllowed("http://127.0.0.1:5500");
    }

    private void assertPreflightAllowed(String origin) throws Exception {
        MockMvc mockMvc = webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(options(PREFERENCES_ENDPOINT)
                        .header(HttpHeaders.ORIGIN, origin)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }
}
