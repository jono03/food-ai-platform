package com.example.foodaiplatformserver.common.exception;

import com.example.foodaiplatformserver.common.ApiErrorCode;

public class ForbiddenException extends ApiException {

    public ForbiddenException() {
        super(ApiErrorCode.FORBIDDEN);
    }

    public ForbiddenException(String message) {
        super(ApiErrorCode.FORBIDDEN, message);
    }
}
