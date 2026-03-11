package com.example.meezy.bc.user.user.infrastructure.security.oauth.handler;

import com.example.meezy.bc.user.user.application.service.internal.TokenService;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.infrastructure.security.auth.AuthDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final String frontendUrl;

    public OauthSuccessHandler(
            TokenService tokenService,
            @Value("${oauth.redirect.frontend-url}") String frontendUrl
    ) {
        this.tokenService = tokenService;
        this.frontendUrl = frontendUrl;
    }

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
        boolean isProfileCompleted = user.isProfileCompleted();

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .queryParam("isProfileCompleted", isProfileCompleted)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
