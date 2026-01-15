package com.example.meezy.config.websocket;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat.websocket")
public record WebSocketProperties(
        String endpoint,
        String brokerPrefix,
        String appPrefix,
        String allowedOrigins
) {
}
