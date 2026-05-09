package com.example.foodaiflatformserver.auth.service;

import com.example.foodaiflatformserver.auth.dto.AuthSignupRequest;
import com.example.foodaiflatformserver.auth.exception.DuplicateEmailException;
import com.example.foodaiflatformserver.auth.repository.UserAccountRepository;
import com.example.foodaiflatformserver.auth.security.JwtTokenProvider;
import com.example.foodaiflatformserver.common.support.KeyedLockExecutor;
import com.example.foodaiflatformserver.user.service.CurrentUserProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserAccountRepository userAccountRepository = mock(UserAccountRepository.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final CurrentUserProvider currentUserProvider = mock(CurrentUserProvider.class);
    private final AuthService authService = new AuthService(
            userAccountRepository,
            jwtTokenProvider,
            currentUserProvider,
            new KeyedLockExecutor()
    );

    @DisplayName("회원가입 저장 중 유니크 제약 충돌이 나면 중복 이메일 예외로 변환한다")
    @Test
    void convertsDuplicateEmailConstraintToConflict() {
        AuthSignupRequest request = new AuthSignupRequest("홍길동", "user@example.com", "password123!");
        when(userAccountRepository.existsByEmail(request.email())).thenReturn(false);
        when(userAccountRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 가입된 이메일입니다.");
    }
}
