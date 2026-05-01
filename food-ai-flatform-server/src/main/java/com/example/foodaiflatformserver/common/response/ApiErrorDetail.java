package com.example.foodaiflatformserver.common.response;

public record ApiErrorDetail(
        String field,
        String reason
) {
}
