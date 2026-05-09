package com.example.foodaiplatformserver.user.service;

import com.example.foodaiplatformserver.auth.security.AuthenticationContext;
import com.example.foodaiplatformserver.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RequestCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        HttpServletRequest request = attributes.getRequest();
        Object currentUserId = request.getAttribute(AuthenticationContext.CURRENT_USER_ID);
        if (currentUserId == null) {
            throw new UnauthorizedException("인증이 필요합니다.");
        }

        return (Long) currentUserId;
    }
}
