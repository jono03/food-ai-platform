package com.example.foodaiplatformserver.auth.exception;

import com.example.foodaiplatformserver.common.exception.ConflictException;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("이미 가입된 이메일입니다. email=" + email);
    }
}
