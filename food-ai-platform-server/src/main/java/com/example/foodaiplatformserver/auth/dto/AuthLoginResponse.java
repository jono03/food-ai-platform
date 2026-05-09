package com.example.foodaiplatformserver.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthLoginResponse(
        @JsonProperty("message")
        String message,

        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("user")
        AuthUserResponse user
) {
}
