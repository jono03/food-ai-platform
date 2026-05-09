package com.example.foodaiplatformserver.auth.exception;

import com.example.foodaiplatformserver.common.exception.UnauthorizedException;

public class AuthenticationFailedException extends UnauthorizedException {

    public AuthenticationFailedException() {
        super("이메일 또는 비밀번호가 일치하지 않습니다.");
    }
}
