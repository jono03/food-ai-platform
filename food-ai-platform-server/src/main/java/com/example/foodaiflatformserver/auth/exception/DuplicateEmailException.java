package com.example.foodaiflatformserver.auth.exception;

import com.example.foodaiflatformserver.common.exception.ConflictException;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("이미 가입된 이메일입니다. email=" + email);
    }
}
