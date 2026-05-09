package com.example.foodaiflatformserver.auth.exception;

import com.example.foodaiflatformserver.common.exception.UnauthorizedException;

public class InvalidTokenException extends UnauthorizedException {

    public InvalidTokenException(String message) {
        super(message);
    }
}
