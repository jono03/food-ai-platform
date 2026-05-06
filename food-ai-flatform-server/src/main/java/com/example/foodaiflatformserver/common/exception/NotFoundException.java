package com.example.foodaiflatformserver.common.exception;

import com.example.foodaiflatformserver.common.ApiErrorCode;

public class NotFoundException extends ApiException {

    public NotFoundException(String message) {
        super(ApiErrorCode.NOT_FOUND, message);
    }
}
