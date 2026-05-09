package com.example.foodaiflatformserver.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthUserResponse(
        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("username")
        String username,

        @JsonProperty("email")
        String email
) {
}
