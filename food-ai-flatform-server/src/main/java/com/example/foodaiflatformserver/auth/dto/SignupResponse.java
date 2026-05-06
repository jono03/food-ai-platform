package com.example.foodaiflatformserver.auth.dto;

public record SignupResponse(
        String message,
        AuthUserResponse user
) {

    public static SignupResponse of(AuthUserResponse user) {
        return new SignupResponse("회원가입이 완료되었습니다.", user);
    }
}
