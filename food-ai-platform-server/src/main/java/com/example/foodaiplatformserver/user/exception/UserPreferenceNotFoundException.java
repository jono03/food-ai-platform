package com.example.foodaiplatformserver.user.exception;

import com.example.foodaiplatformserver.common.exception.NotFoundException;

public class UserPreferenceNotFoundException extends NotFoundException {

    public UserPreferenceNotFoundException(Long userId) {
        super("사용자 취향 정보를 찾을 수 없습니다. user_id=" + userId);
    }
}
