package com.example.foodaiflatformserver.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthSignupResponse(
        @JsonProperty("message")
        String message,

        @JsonProperty("user")
        AuthUserResponse user
) {
}
