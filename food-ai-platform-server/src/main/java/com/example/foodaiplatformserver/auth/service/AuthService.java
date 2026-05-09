package com.example.foodaiplatformserver.auth.service;

import com.example.foodaiplatformserver.auth.dto.AuthLoginRequest;
import com.example.foodaiplatformserver.auth.dto.AuthLoginResponse;
import com.example.foodaiplatformserver.auth.dto.AuthSignupRequest;
import com.example.foodaiplatformserver.auth.dto.AuthSignupResponse;
import com.example.foodaiplatformserver.auth.dto.AuthUserResponse;
import com.example.foodaiplatformserver.auth.entity.UserAccount;
import com.example.foodaiplatformserver.auth.exception.AuthenticationFailedException;
import com.example.foodaiplatformserver.auth.exception.DuplicateEmailException;
import com.example.foodaiplatformserver.auth.exception.UserAccountNotFoundException;
import com.example.foodaiplatformserver.auth.repository.UserAccountRepository;
import com.example.foodaiplatformserver.auth.security.JwtTokenProvider;
import com.example.foodaiplatformserver.common.support.KeyedLockExecutor;
import com.example.foodaiplatformserver.user.service.CurrentUserProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentUserProvider currentUserProvider;
    private final KeyedLockExecutor keyedLockExecutor;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserAccountRepository userAccountRepository,
                       JwtTokenProvider jwtTokenProvider,
                       CurrentUserProvider currentUserProvider,
                       KeyedLockExecutor keyedLockExecutor) {
        this.userAccountRepository = userAccountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.currentUserProvider = currentUserProvider;
        this.keyedLockExecutor = keyedLockExecutor;
    }

    @Transactional
    public AuthSignupResponse signup(AuthSignupRequest request) {
        return keyedLockExecutor.execute("signup:" + request.email(), () -> {
            if (userAccountRepository.existsByEmail(request.email())) {
                throw new DuplicateEmailException(request.email());
            }

            UserAccount userAccount = new UserAccount(
                    request.username(),
                    request.email(),
                    passwordEncoder.encode(request.password())
            );

            try {
                UserAccount saved = userAccountRepository.saveAndFlush(userAccount);
                return new AuthSignupResponse("회원가입이 완료되었습니다.", toUserResponse(saved));
            } catch (DataIntegrityViolationException exception) {
                if (userAccountRepository.existsByEmail(request.email())) {
                    throw new DuplicateEmailException(request.email());
                }
                throw exception;
            }
        });
    }

    @Transactional(readOnly = true)
    public AuthLoginResponse login(AuthLoginRequest request) {
        UserAccount userAccount = userAccountRepository.findByEmail(request.email())
                .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .orElseThrow(AuthenticationFailedException::new);

        String token = jwtTokenProvider.createToken(userAccount.getId(), userAccount.getEmail());
        return new AuthLoginResponse("로그인 성공", token, "Bearer", toUserResponse(userAccount));
    }

    @Transactional(readOnly = true)
    public AuthUserResponse me() {
        Long userId = currentUserProvider.getCurrentUserId();
        UserAccount userAccount = userAccountRepository.findById(userId)
                .orElseThrow(() -> new UserAccountNotFoundException(userId));
        return toUserResponse(userAccount);
    }

    private AuthUserResponse toUserResponse(UserAccount userAccount) {
        return new AuthUserResponse(userAccount.getId(), userAccount.getUsername(), userAccount.getEmail());
    }
}
