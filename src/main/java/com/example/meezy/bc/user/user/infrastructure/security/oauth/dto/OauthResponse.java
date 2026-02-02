package com.example.meezy.bc.user.user.infrastructure.security.oauth.dto;

public record OauthResponse(
        String accessToken,
        String refreshToken,
        boolean isProfileCompleted
) {
}

