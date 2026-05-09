package com.example.foodaiflatformserver.auth.security;

import com.example.foodaiflatformserver.auth.dto.AuthenticatedUser;
import com.example.foodaiflatformserver.auth.exception.InvalidTokenException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final byte[] secretKey;
    private final long expirationSeconds;

    public JwtTokenProvider(ObjectMapper objectMapper,
                            @Value("${app.jwt.secret:food-ai-platform-secret-key-change-me}") String secret,
                            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.objectMapper = objectMapper;
        this.secretKey = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(Long userId, String email) {
        try {
            long issuedAt = Instant.now().getEpochSecond();
            long expiresAt = issuedAt + expirationSeconds;

            String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
            Map<String, Object> payloadMap = new LinkedHashMap<>();
            payloadMap.put("sub", String.valueOf(userId));
            payloadMap.put("email", email);
            payloadMap.put("iat", issuedAt);
            payloadMap.put("exp", expiresAt);
            String payload = encodeJson(payloadMap);

            String signature = sign(header + "." + payload);
            return header + "." + payload + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("JWT 토큰 생성에 실패했습니다.", exception);
        }
    }

    public AuthenticatedUser parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new InvalidTokenException("토큰 형식이 올바르지 않습니다.");
            }

            String expectedSignature = sign(parts[0] + "." + parts[1]);
            if (!expectedSignature.equals(parts[2])) {
                throw new InvalidTokenException("토큰 서명이 올바르지 않습니다.");
            }

            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), MAP_TYPE);
            long expiresAt = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > expiresAt) {
                throw new InvalidTokenException("토큰이 만료되었습니다.");
            }

            Long userId = Long.valueOf(String.valueOf(payload.get("sub")));
            String email = String.valueOf(payload.get("email"));
            return new AuthenticatedUser(userId, email);
        } catch (InvalidTokenException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidTokenException("토큰이 올바르지 않습니다.");
        }
    }

    private String encodeJson(Map<String, Object> payload) throws Exception {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
        return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
