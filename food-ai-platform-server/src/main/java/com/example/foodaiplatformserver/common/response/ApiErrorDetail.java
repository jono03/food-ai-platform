package com.example.foodaiplatformserver.common.response;

public record ApiErrorDetail(
        String field,
        String reason
) {
}
