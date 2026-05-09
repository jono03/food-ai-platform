package com.example.foodaiflatformserver.auth.dto;

public record AuthenticatedUser(
        Long userId,
        String email
) {
}
