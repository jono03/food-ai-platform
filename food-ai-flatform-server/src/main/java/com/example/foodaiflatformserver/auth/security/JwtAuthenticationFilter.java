package com.example.foodaiflatformserver.auth.security;

import com.example.foodaiflatformserver.common.exception.UnauthorizedException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_ENDPOINTS = Set.of(
            "/api/v1/auth/signup",
            "/api/v1/auth/login"
    );

    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return PUBLIC_ENDPOINTS.contains(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!StringUtils.hasText(bearerToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!bearerToken.startsWith("Bearer ")) {
            unauthorized(request, response, "Authorization 헤더 형식이 올바르지 않습니다.");
            return;
        }

        String token = bearerToken.substring(7);

        try {
            jwtProvider.validateToken(token);
            Long userId = jwtProvider.extractUserId(token);
            UserPrincipal principal = userDetailsService.loadUserById(userId);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException | UnauthorizedException exception) {
            unauthorized(request, response, "유효하지 않은 토큰입니다.");
        }
    }

    private void unauthorized(HttpServletRequest request, HttpServletResponse response, String message)
            throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        request.setAttribute(RestAuthenticationEntryPoint.AUTH_ERROR_MESSAGE_ATTRIBUTE, message);
        authenticationEntryPoint.commence(request, response, new BadCredentialsException(message));
    }
}
