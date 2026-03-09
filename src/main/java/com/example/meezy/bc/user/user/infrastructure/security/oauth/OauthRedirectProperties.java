package com.example.meezy.bc.user.user.infrastructure.security.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.redirect")
public record OauthRedirectProperties(
        String frontendUrl
) {
}
