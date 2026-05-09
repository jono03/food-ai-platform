package com.example.foodaiplatformserver.auth.dto;

public record AuthenticatedUser(
        Long userId,
        String email
) {
}
