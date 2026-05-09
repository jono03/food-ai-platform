package com.example.foodaiflatformserver.auth.exception;

import com.example.foodaiflatformserver.common.exception.UnauthorizedException;

public class AuthenticationFailedException extends UnauthorizedException {

    public AuthenticationFailedException() {
        super("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
