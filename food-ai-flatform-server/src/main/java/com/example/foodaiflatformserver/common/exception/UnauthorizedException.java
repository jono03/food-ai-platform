package com.example.foodaiflatformserver.common.exception;

import com.example.foodaiflatformserver.common.ApiErrorCode;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String message) {
        super(ApiErrorCode.UNAUTHORIZED, message);
    }
}
