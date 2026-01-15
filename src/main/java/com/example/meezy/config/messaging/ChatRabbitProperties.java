package com.example.meezy.config.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chat.rabbit")
public record ChatRabbitProperties(
        String exchangeName,
        String queuePrefix
) {
}
