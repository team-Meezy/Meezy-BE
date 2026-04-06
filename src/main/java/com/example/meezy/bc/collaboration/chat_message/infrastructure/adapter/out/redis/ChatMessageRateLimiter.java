package com.example.meezy.bc.collaboration.chat_message.infrastructure.adapter.out.redis;

import com.example.meezy.bc.collaboration.chat_message.application.port.out.ChatMessageRateLimitPort;
import com.example.meezy.bc.collaboration.chat_message.infrastructure.adapter.exception.ChatMessageRateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChatMessageRateLimiter implements ChatMessageRateLimitPort {

    private static final long WINDOW_SECONDS = 1;
    private static final long MAX_MESSAGES_PER_WINDOW = 5;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setScriptText(
                "local count = redis.call('INCR', KEYS[1]) " +
                "if count == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                "return count"
        );
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redisTemplate;

    public void validate(UUID userId, UUID chatRoomId) {
        String key = "chat:rate:%s:%s".formatted(userId, chatRoomId);

        Long count = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(key),
                String.valueOf(WINDOW_SECONDS)
        );

        if (count == null || count > MAX_MESSAGES_PER_WINDOW) {
            throw new ChatMessageRateLimitExceededException();
        }
    }
}
