package com.example.meezy.bc.user.user.infrastructure.security.util;

import com.example.meezy.bc.user.user.infrastructure.security.jwt.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenResolver {

    private final JwtProperties jwtProperties;

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(jwtProperties.header());
        return parseToken(bearerToken);
    }

    private String parseToken(String bearerToken){
        if(bearerToken != null && bearerToken.startsWith(jwtProperties.prefix())){
            return bearerToken.replace(jwtProperties.prefix(), "").trim();
        }
        return null;
    }
}
