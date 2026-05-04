package com.example.foodaiflatformserver.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {

    private final String secret;
    private final long accessTokenExpirationSeconds;
    private SecretKey secretKey;

    public JwtProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds
    ) {
        this.secret = secret;
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    @PostConstruct
    void init() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (RuntimeException exception) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
                .subject(String.valueOf(principal.id()))
                .claim("email", principal.email())
                .claim("username", principal.username())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public Long extractUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    public void validateToken(String token) {
        parseClaims(token);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
