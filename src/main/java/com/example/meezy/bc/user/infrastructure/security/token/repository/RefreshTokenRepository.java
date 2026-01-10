package com.example.meezy.bc.user.infrastructure.security.token.repository;

import com.example.meezy.bc.user.infrastructure.security.token.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByAccountId(String accountId);
}
