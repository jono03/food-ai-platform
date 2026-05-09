package com.example.foodaiplatformserver.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequest(
        @JsonProperty("email")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @JsonProperty("password")
        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
) {
}
