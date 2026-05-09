package com.example.foodaiplatformserver.common.exception;

import com.example.foodaiplatformserver.common.ApiErrorCode;

public class BadRequestException extends ApiException {

    public BadRequestException() {
        super(ApiErrorCode.INVALID_REQUEST);
    }

    public BadRequestException(String message) {
        super(ApiErrorCode.INVALID_REQUEST, message);
    }
}
