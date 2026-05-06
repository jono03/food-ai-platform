package com.example.foodaiflatformserver.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        @JsonProperty("message")
        String message,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("user")
        AuthUserResponse user
) {

    public static LoginResponse of(String accessToken, AuthUserResponse user) {
        return new LoginResponse("로그인 성공", accessToken, "Bearer", user);
    }
}
