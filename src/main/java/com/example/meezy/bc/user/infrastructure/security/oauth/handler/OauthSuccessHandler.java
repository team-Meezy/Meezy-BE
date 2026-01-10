package com.example.meezy.bc.user.infrastructure.security.oauth.handler;

import com.example.meezy.bc.user.application.service.internal.TokenService;
import com.example.meezy.bc.user.domain.User;
import com.example.meezy.bc.user.infrastructure.security.oauth.dto.OauthResponse;
import com.example.meezy.bc.user.infrastructure.security.auth.AuthDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OauthSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        User user = authDetails.getUser();

        String userId = user.getUserId().value().toString();

        OauthResponse oauthResponse = new OauthResponse(
                tokenService.generateAccessToken(userId, user.getOauthProvider()),
                tokenService.generateRefreshToken(userId, user.getOauthProvider()),
                false
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(oauthResponse));
    }
}
