package com.example.foodaiflatformserver.common.exception;

import com.example.foodaiflatformserver.common.ApiErrorCode;

public class BadRequestException extends ApiException {

    public BadRequestException() {
        super(ApiErrorCode.INVALID_REQUEST);
    }

    public BadRequestException(String message) {
        super(ApiErrorCode.INVALID_REQUEST, message);
    }
}
