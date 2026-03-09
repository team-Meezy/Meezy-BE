package com.example.meezy.bc.user.user.infrastructure.security.oauth.handler;

import com.example.meezy.bc.user.user.application.service.internal.TokenService;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.infrastructure.security.auth.AuthDetails;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.OauthRedirectProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final OauthRedirectProperties oauthRedirectProperties;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        User user = authDetails.getUser();

        String userId = user.getUserId().value().toString();

        String accessToken = tokenService.generateAccessToken(userId, user.getOauthProvider());
        String refreshToken = tokenService.generateRefreshToken(userId, user.getOauthProvider());

        String redirectUrl = UriComponentsBuilder
                .fromUriString(oauthRedirectProperties.frontendUrl() + "/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("isProfileCompleted", false)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
