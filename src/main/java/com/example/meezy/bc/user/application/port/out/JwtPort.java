package com.example.meezy.bc.user.application.port.out;

import com.example.meezy.bc.user.domain.type.OauthProvider;

public interface JwtPort {

    String generateAccessToken(String userId, OauthProvider provider);
    String generateRefreshToken(String userId, OauthProvider provider);
    String extractSubject(String token);
    Long getRefreshTokenExp();
    OauthProvider getOauthProvider(String token);
}
