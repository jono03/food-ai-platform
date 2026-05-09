package com.example.foodaiplatformserver.auth.exception;

import com.example.foodaiplatformserver.common.exception.NotFoundException;

public class UserAccountNotFoundException extends NotFoundException {

    public UserAccountNotFoundException(Long userId) {
        super("사용자를 찾을 수 없습니다. user_id=" + userId);
    }
}
