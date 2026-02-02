package com.example.meezy.config.websocket;

import com.example.meezy.bc.user.user.application.service.internal.TokenService;
import com.example.meezy.bc.user.user.infrastructure.security.auth.AuthDetails;
import com.example.meezy.bc.user.user.infrastructure.security.jwt.JwtProperties;
import com.example.meezy.bc.user.user.infrastructure.security.jwt.exception.InvalidJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor, ExecutorChannelInterceptor {

    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel){
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class); //Message 에서 STOMP 헤더 접근자 추출

        if (accessor == null) return message;

        if(StompCommand.CONNECT.equals(accessor.getCommand())){
            authenticate(accessor); //STOMP CONNECT 프레임인 경우
        } else if (accessor.getUser() instanceof Authentication auth) {
            SecurityContextHolder.getContext().setAuthentication(auth); //CONNECT 이후의 메시지이고, 이미 인증 정보가 있다면 재사용
        }

        return message;
    }

    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        //새 스레드일 가능성이 존재하기 때문
        Optional.ofNullable(MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class))
                .map(StompHeaderAccessor::getUser)
                .filter(Authentication.class::isInstance)
                .map(Authentication.class::cast)
                .ifPresent(auth -> SecurityContextHolder.getContext().setAuthentication(auth));

        return message;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = extractToken(accessor.getFirstNativeHeader(jwtProperties.header()));
        if (token == null) throw new InvalidJwtException();

        try {
            AuthDetails details = new AuthDetails(tokenService.authenticate(token));
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    details, null, details.getAuthorities());
            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.warn("JWT 인증 실패 : {}", e.getMessage());
            throw new InvalidJwtException();
        }
    }

    private String extractToken(String bearerToken) {
        return (bearerToken != null && bearerToken.startsWith(jwtProperties.prefix()))
                ? bearerToken.substring(jwtProperties.prefix().length()).trim()
                : null;
    }
}
