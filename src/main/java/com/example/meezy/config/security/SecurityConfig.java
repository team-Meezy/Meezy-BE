package com.example.meezy.config.security;

import com.example.meezy.bc.user.user.application.service.internal.TokenService;
import com.example.meezy.bc.user.user.infrastructure.security.jwt.JwtTokenFilter;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.CustomOauthService;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.handler.OauthFailureHandler;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.handler.OauthSuccessHandler;
import com.example.meezy.bc.user.user.infrastructure.security.util.TokenResolver;
import com.example.meezy.config.exception.GlobalExceptionFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenResolver tokenResolver;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http, CustomOauthService customOauthService,
            OauthSuccessHandler successHandler, OauthFailureHandler failureHandler
    ) throws Exception{
        return http
                .csrf(CsrfConfigurer::disable)
                .cors(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOauthService))
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .redirectionEndpoint(endpoint -> endpoint
                                .baseUri("/login/oauth2/code/{registrationId}")))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/oauth2/code/**", "/oauth2/authorization/**").permitAll()
                        .requestMatchers("/ws-chat/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/reissue").permitAll()
                        .requestMatchers(HttpMethod.POST, "/email/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/images/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtTokenFilter(tokenResolver, tokenService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new GlobalExceptionFilter(objectMapper), JwtTokenFilter.class)
                .build();

    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
