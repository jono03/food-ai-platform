package com.example.foodaiflatformserver.common.exception;

import com.example.foodaiflatformserver.common.ApiErrorCode;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(ApiErrorCode.FORBIDDEN, message);
    }
}
