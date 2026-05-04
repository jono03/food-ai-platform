package com.example.foodaiflatformserver.auth.security;

import com.example.foodaiflatformserver.common.exception.UnauthorizedException;
import com.example.foodaiflatformserver.user.entity.User;
import com.example.foodaiflatformserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .map(UserPrincipal::from)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    public UserPrincipal loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("유효하지 않은 토큰입니다."));
        return UserPrincipal.from(user);
    }
}
