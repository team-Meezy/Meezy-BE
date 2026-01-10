package com.example.meezy.bc.user.infrastructure.security.oauth.dto;

import com.example.meezy.bc.user.domain.type.OauthProvider;

import java.util.Map;

public record OauthUserInfo(
        String email,
        OauthProvider provider,
        Map<String, Object> attributes
) {
}
