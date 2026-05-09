package com.example.foodaiplatformserver.common.exception;

import com.example.foodaiplatformserver.common.ApiErrorCode;

public class UnauthorizedException extends ApiException {

    public UnauthorizedException() {
        super(ApiErrorCode.UNAUTHORIZED);
    }

    public UnauthorizedException(String message) {
        super(ApiErrorCode.UNAUTHORIZED, message);
    }
}
