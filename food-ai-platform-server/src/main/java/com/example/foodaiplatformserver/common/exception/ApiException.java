package com.example.foodaiplatformserver.common.exception;

import com.example.foodaiplatformserver.common.ApiErrorCode;

public class ApiException extends RuntimeException {

    private final ApiErrorCode errorCode;

    public ApiException(ApiErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ApiException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }
}
