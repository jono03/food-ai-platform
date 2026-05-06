package com.example.foodaiflatformserver.auth.service;

import com.example.foodaiflatformserver.auth.dto.AuthUserResponse;
import com.example.foodaiflatformserver.auth.dto.LoginRequest;
import com.example.foodaiflatformserver.auth.dto.LoginResponse;
import com.example.foodaiflatformserver.auth.dto.SignupRequest;
import com.example.foodaiflatformserver.auth.dto.SignupResponse;
import com.example.foodaiflatformserver.auth.security.JwtProvider;
import com.example.foodaiflatformserver.auth.security.UserPrincipal;
import com.example.foodaiflatformserver.common.exception.ConflictException;
import com.example.foodaiflatformserver.common.exception.UnauthorizedException;
import com.example.foodaiflatformserver.user.entity.User;
import com.example.foodaiflatformserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User savedUser = userRepository.save(user);
        return SignupResponse.of(AuthUserResponse.from(savedUser));
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new UnauthorizedException("이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken = jwtProvider.generateAccessToken(principal);

        return LoginResponse.of(accessToken, AuthUserResponse.from(user));
    }

    public AuthUserResponse getMe(UserPrincipal principal) {
        return new AuthUserResponse(principal.id(), principal.username(), principal.email());
    }
}
