package com.example.foodaiplatformserver.common.response;

import com.example.foodaiplatformserver.common.ApiErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code,
        String message,
        List<ApiErrorDetail> details
) {

    public static ApiErrorResponse of(ApiErrorCode errorCode, String message, List<ApiErrorDetail> details) {
        return new ApiErrorResponse(
                LocalDateTime.now(),
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                message,
                details
        );
    }

    public static ApiErrorResponse of(ApiErrorCode errorCode, List<ApiErrorDetail> details) {
        return of(errorCode, errorCode.getMessage(), details);
    }

    public static ApiErrorResponse of(ApiErrorCode errorCode, String message) {
        return of(errorCode, message, List.of());
    }
}
