package com.example.meezy.bc.collaboration.chat_message.infrastructure.adapter.out.redis;

import com.example.meezy.bc.collaboration.chat_message.infrastructure.adapter.exception.ChatMessageRateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatMessageRateLimiter {

    private static final long WINDOW_SECONDS = 1;
    private static final long MAX_MESSAGES_PER_WINDOW = 5;

    private final StringRedisTemplate redisTemplate;

    public void validate(UUID userId, UUID chatRoomId) {
        String key = "chat:rate:%s:%s".formatted(userId, chatRoomId);
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            return;
        }

        if (count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        if (count > MAX_MESSAGES_PER_WINDOW) {
            throw new ChatMessageRateLimitExceededException();
        }
    }
}
