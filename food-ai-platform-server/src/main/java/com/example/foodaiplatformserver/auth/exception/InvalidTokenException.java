package com.example.foodaiplatformserver.auth.exception;

import com.example.foodaiplatformserver.common.exception.UnauthorizedException;

public class InvalidTokenException extends UnauthorizedException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
