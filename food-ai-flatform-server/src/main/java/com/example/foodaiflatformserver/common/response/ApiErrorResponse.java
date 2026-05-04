package com.example.foodaiflatformserver.common.response;

import com.example.foodaiflatformserver.common.ApiErrorCode;

import java.util.List;

public record ApiErrorResponse(
        String timestamp,
        int status,
        String code,
        String message,
        List<ApiErrorDetail> details
) {

    public static ApiErrorResponse of(ApiErrorCode errorCode, String message) {
        return of(errorCode, message, List.of());
    }

    public static ApiErrorResponse of(ApiErrorCode errorCode, String message, List<ApiErrorDetail> details) {
        return new ApiErrorResponse(
                java.time.LocalDateTime.now().toString(),
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                message,
                details
        );
    }
}
