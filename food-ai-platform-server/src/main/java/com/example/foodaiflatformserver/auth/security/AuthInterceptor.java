package com.example.foodaiflatformserver.auth.security;

import com.example.foodaiflatformserver.auth.dto.AuthenticatedUser;
import com.example.foodaiflatformserver.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public AuthInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        AuthenticatedUser authenticatedUser = jwtTokenProvider.parse(token);
        request.setAttribute(AuthenticationContext.CURRENT_USER_ID, authenticatedUser.userId());
        request.setAttribute(AuthenticationContext.CURRENT_USER_EMAIL, authenticatedUser.email());
        return true;
    }
}
