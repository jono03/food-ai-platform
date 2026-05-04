package com.example.foodaiflatformserver.common.exception;

import com.example.foodaiflatformserver.common.ApiErrorCode;
import lombok.Getter;

@Getter
public abstract class ApiException extends RuntimeException {

    private final ApiErrorCode errorCode;

    protected ApiException(ApiErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
