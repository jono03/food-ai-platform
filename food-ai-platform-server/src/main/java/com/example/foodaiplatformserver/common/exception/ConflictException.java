package com.example.foodaiplatformserver.common.exception;

import com.example.foodaiplatformserver.common.ApiErrorCode;

public class ConflictException extends ApiException {

    public ConflictException() {
        super(ApiErrorCode.CONFLICT);
    }

    public ConflictException(String message) {
        super(ApiErrorCode.CONFLICT, message);
    }
}
