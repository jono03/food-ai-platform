package com.example.foodaiplatformserver.common.exception;

import com.example.foodaiplatformserver.common.ApiErrorCode;

public class NotFoundException extends ApiException {

    public NotFoundException() {
        super(ApiErrorCode.NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(ApiErrorCode.NOT_FOUND, message);
    }
}
