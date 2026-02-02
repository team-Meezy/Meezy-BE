package com.example.meezy.bc.user.user.application.service.internal;

import com.example.meezy.bc.user.user.application.port.out.JwtPort;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtPort jwtPort;
    private final UserRepository userRepository;

    public String generateAccessToken(String userId, OauthProvider provider){
        return jwtPort.generateAccessToken(userId, provider);
    }

    public String generateRefreshToken(String userId, OauthProvider provider){
        return jwtPort.generateRefreshToken(userId, provider);
    }

    public User authenticate(String token){
        String userId = jwtPort.extractSubject(token);
        return userRepository.findByUserId_Value(UUID.fromString(userId))
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));
    }
}
