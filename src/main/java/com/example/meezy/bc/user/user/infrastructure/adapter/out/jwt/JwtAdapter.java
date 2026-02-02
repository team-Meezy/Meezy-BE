package com.example.meezy.bc.user.user.infrastructure.adapter.out.jwt;

import com.example.meezy.bc.user.user.application.port.out.JwtPort;
import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import com.example.meezy.bc.user.user.infrastructure.security.jwt.JwtProperties;
import com.example.meezy.bc.user.user.infrastructure.security.jwt.exception.ExpiredJwtException;
import com.example.meezy.bc.user.user.infrastructure.security.jwt.exception.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtAdapter implements JwtPort {

    private final JwtProperties jwtProperties;

    private SecretKey getSecretKey(){
        return Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(String userId, OauthProvider provider) {
        return generateToken(userId, provider, "access_token", jwtProperties.accessExp());
    }

    @Override
    public String generateRefreshToken(String userId, OauthProvider provider) {
        return generateToken(userId, provider, "refresh_token", jwtProperties.refreshExp());
    }

    @Override
    public String extractSubject(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public Long getRefreshTokenExp() {
        return jwtProperties.refreshExp();
    }

    @Override
    public OauthProvider getOauthProvider(String token) {
        return getClaims(token).get("provider", OauthProvider.class);
    }

    private String generateToken(String userId, OauthProvider provider, String type, Long exp){
        return Jwts.builder()
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .setSubject(userId)
                .claim("type", type)
                .claim("provider", provider)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + exp * 1000))
                .compact();
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new ExpiredJwtException();
        } catch (Exception e) {
            throw new InvalidJwtException();
        }
    }
}
