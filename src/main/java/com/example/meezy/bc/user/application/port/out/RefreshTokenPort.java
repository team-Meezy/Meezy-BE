package com.example.meezy.bc.user.application.port.out;

import java.util.Optional;

public interface RefreshTokenPort {

    void save(String userId, String refreshToken, long ttlSeconds);
    Optional<String> findByToken(String refreshToken);
}
