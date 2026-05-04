package com.example.foodaiflatformserver.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApiErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "요청 값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    CONFLICT(HttpStatus.CONFLICT, "CONFLICT", "리소스 충돌이 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
