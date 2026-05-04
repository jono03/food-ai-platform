package com.example.foodaiflatformserver.common.exception;

import com.example.foodaiflatformserver.common.ApiErrorCode;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(ApiErrorCode.CONFLICT, message);
    }
}
