package com.example.foodaiflatformserver.auth.service;

import com.example.foodaiflatformserver.auth.dto.AuthLoginRequest;
import com.example.foodaiflatformserver.auth.dto.AuthLoginResponse;
import com.example.foodaiflatformserver.auth.dto.AuthSignupRequest;
import com.example.foodaiflatformserver.auth.dto.AuthSignupResponse;
import com.example.foodaiflatformserver.auth.dto.AuthUserResponse;
import com.example.foodaiflatformserver.auth.entity.UserAccount;
import com.example.foodaiflatformserver.auth.exception.AuthenticationFailedException;
import com.example.foodaiflatformserver.auth.exception.DuplicateEmailException;
import com.example.foodaiflatformserver.auth.exception.UserAccountNotFoundException;
import com.example.foodaiflatformserver.auth.repository.UserAccountRepository;
import com.example.foodaiflatformserver.auth.security.JwtTokenProvider;
import com.example.foodaiflatformserver.user.service.CurrentUserProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentUserProvider currentUserProvider;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserAccountRepository userAccountRepository,
                       JwtTokenProvider jwtTokenProvider,
                       CurrentUserProvider currentUserProvider) {
        this.userAccountRepository = userAccountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public AuthSignupResponse signup(AuthSignupRequest request) {
        if (userAccountRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        UserAccount userAccount = new UserAccount(
                request.username(),
                request.email(),
                passwordEncoder.encode(request.password())
        );
        UserAccount saved = userAccountRepository.save(userAccount);

        return new AuthSignupResponse("회원가입이 완료되었습니다.", toUserResponse(saved));
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
