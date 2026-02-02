package com.example.meezy.bc.user.user.application.service;

import com.example.meezy.bc.user.user.application.port.out.PasswordVerifierPort;
import com.example.meezy.bc.user.user.application.service.dto.request.LoginRequest;
import com.example.meezy.bc.user.user.application.service.dto.response.TokenResponse;
import com.example.meezy.bc.user.user.application.service.exception.PasswordMisMatchException;
import com.example.meezy.bc.user.user.application.service.exception.UserNotFoundException;
import com.example.meezy.bc.user.user.application.service.internal.TokenService;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalLoginService {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordVerifierPort passwordVerifierPort;

    @Transactional
    public TokenResponse login(LoginRequest request){
        User user = userRepository.findByAccountId(request.accountId())
                .orElseThrow(UserNotFoundException::new);

        if(user.getPassword() == null || !passwordVerifierPort.matches(request.password(), user.getPassword())){
            throw new PasswordMisMatchException();
        }

        String userId = user.getUserId().value().toString();

        return TokenResponse.builder()
                .accessToken(tokenService.generateAccessToken(userId, user.getOauthProvider()))
                .refreshToken(tokenService.generateRefreshToken(userId, user.getOauthProvider()))
                .build();
    }
}
