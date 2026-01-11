package com.example.meezy.bc.user.application.service;

import com.example.meezy.bc.user.application.port.out.JwtPort;
import com.example.meezy.bc.user.application.port.out.RefreshTokenPort;
import com.example.meezy.bc.user.application.service.dto.request.TokenReissueRequest;
import com.example.meezy.bc.user.application.service.dto.response.TokenResponse;
import com.example.meezy.bc.user.application.service.exception.RefreshTokenNotFoundException;
import com.example.meezy.bc.user.application.service.internal.TokenService;
import com.example.meezy.bc.user.domain.type.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final RefreshTokenPort refreshTokenPort;
    private final JwtPort jwtPort;
    private final TokenService tokenService;

    @Transactional
    public TokenResponse reissue(TokenReissueRequest request){
        String refreshToken = refreshTokenPort.findByToken(request.refreshToken())
                .orElseThrow(RefreshTokenNotFoundException::new);

        String userId = jwtPort.extractSubject(refreshToken);
        OauthProvider provider = jwtPort.getOauthProvider(refreshToken);

        return TokenResponse.builder()
                .accessToken(tokenService.generateAccessToken(userId, provider))
                .refreshToken(tokenService.generateRefreshToken(userId, provider))
                .build();
    }
}
