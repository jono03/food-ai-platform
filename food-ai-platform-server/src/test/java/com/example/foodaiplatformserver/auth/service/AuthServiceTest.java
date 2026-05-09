package com.example.foodaiplatformserver.auth.service;

import com.example.foodaiplatformserver.auth.dto.AuthSignupRequest;
import com.example.foodaiplatformserver.auth.exception.DuplicateEmailException;
import com.example.foodaiplatformserver.auth.repository.UserAccountRepository;
import com.example.foodaiplatformserver.auth.security.JwtTokenProvider;
import com.example.foodaiplatformserver.common.support.KeyedLockExecutor;
import com.example.foodaiplatformserver.user.service.CurrentUserProvider;
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
        when(userAccountRepository.existsByEmail(request.email())).thenReturn(false, true);
        when(userAccountRepository.saveAndFlush(any()))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("이미 가입된 이메일입니다.");
    }

    @DisplayName("중복 이메일이 아닌 저장 오류는 그대로 전파한다")
    @Test
    void rethrowsNonDuplicateDataIntegrityViolation() {
        AuthSignupRequest request = new AuthSignupRequest("홍길동", "user@example.com", "password123!");
        DataIntegrityViolationException exception = new DataIntegrityViolationException("schema mismatch");

        when(userAccountRepository.existsByEmail(request.email())).thenReturn(false, false);
        when(userAccountRepository.saveAndFlush(any())).thenThrow(exception);

        assertThatThrownBy(() -> authService.signup(request))
                .isSameAs(exception);
    }
}
