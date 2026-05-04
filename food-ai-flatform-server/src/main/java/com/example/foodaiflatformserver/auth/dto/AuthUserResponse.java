package com.example.foodaiflatformserver.auth.dto;

import com.example.foodaiflatformserver.user.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthUserResponse(
        @JsonProperty("user_id")
        Long userId,

        @JsonProperty("username")
        String username,

        @JsonProperty("email")
        String email
) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(user.getId(), user.getUsername(), user.getEmail());
    }
}
