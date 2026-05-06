package com.example.foodaiflatformserver.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignupRequest(
        @NotBlank(message = "사용자명은 필수입니다.")
        String username,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$",
                message = "비밀번호는 8~20자의 영문, 숫자, 특수문자를 모두 포함해야 합니다."
        )
        String password
) {
}
