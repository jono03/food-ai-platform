package com.example.foodaiflatformserver.auth.security;

import com.example.foodaiflatformserver.common.ApiErrorCode;
import com.example.foodaiflatformserver.common.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String AUTH_ERROR_MESSAGE_ATTRIBUTE = "auth_error_message";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String message = (String) request.getAttribute(AUTH_ERROR_MESSAGE_ATTRIBUTE);
        if (message == null || message.isBlank()) {
            message = ApiErrorCode.UNAUTHORIZED.getMessage();
        }

        response.setStatus(ApiErrorCode.UNAUTHORIZED.getHttpStatus().value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiErrorResponse.of(ApiErrorCode.UNAUTHORIZED, message));
    }
}
