package com.example.meezy.bc.user.user.infrastructure.security.jwt;

import com.example.meezy.bc.user.user.application.service.internal.TokenService;
import com.example.meezy.bc.user.user.infrastructure.security.auth.AuthDetails;
import com.example.meezy.bc.user.user.infrastructure.security.util.TokenResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final TokenResolver tokenResolver;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String parseToken = tokenResolver.resolveToken(request);

        if(parseToken !=null){
            UserDetails details = new AuthDetails(tokenService.authenticate(parseToken));
            Authentication authentication = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
