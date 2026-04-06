package com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.out.redis;

import com.example.meezy.bc.collaboration.meeting.application.port.out.SignalRateLimitPort;
import com.example.meezy.bc.collaboration.meeting.infrastructure.adapter.exception.SignalRateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SignalRateLimiter implements SignalRateLimitPort {

    private static final long WINDOW_SECONDS = 1;
    private static final long MAX_SIGNALS_PER_WINDOW = 30;

    private final StringRedisTemplate redisTemplate;

    public void validate(UUID userId, UUID teamId) {
        String key = "signal:rate:%s:%s".formatted(userId, teamId);
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            return;
        }

        if (count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(WINDOW_SECONDS));
        }

        if (count > MAX_SIGNALS_PER_WINDOW) {
            throw new SignalRateLimitExceededException();
        }
    }
}
