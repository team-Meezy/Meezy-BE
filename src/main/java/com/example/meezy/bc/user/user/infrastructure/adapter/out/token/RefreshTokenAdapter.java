package com.example.meezy.bc.user.user.infrastructure.adapter.out.token;

import com.example.meezy.bc.user.user.application.port.out.RefreshTokenPort;
import com.example.meezy.bc.user.user.infrastructure.security.token.RefreshToken;
import com.example.meezy.bc.user.user.infrastructure.security.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenAdapter implements RefreshTokenPort {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public void save(String userId, String refreshToken, long ttlSeconds) {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(userId)
                        .refreshToken(refreshToken)
                        .ttl(ttlSeconds)
                        .build()
        );
    }

    @Override
    public Optional<String> findByToken(String refreshToken) {
        return refreshTokenRepository.findByRefreshToken(refreshToken)
                .map(RefreshToken::getRefreshToken);
    }
}
