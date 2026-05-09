package com.example.foodaiflatformserver.user.exception;

import com.example.foodaiflatformserver.common.exception.NotFoundException;

public class UserPreferenceNotFoundException extends NotFoundException {

    public UserPreferenceNotFoundException(Long userId) {
        super("사용자 취향 정보를 찾을 수 없습니다. user_id=" + userId);
    }
}
