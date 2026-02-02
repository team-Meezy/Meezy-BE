package com.example.meezy.bc.collaboration.participation_metrics.infrastructure.adapter.out.redis;

import com.example.meezy.bc.collaboration.participation_metrics.application.port.out.ParticipationCounterPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
@RequiredArgsConstructor
public class ParticipationCounterAdapter implements ParticipationCounterPort {

    private static final String VOICE_KEY_PREFIX = "meeting:%s:voice";
    private static final String CHAT_KEY_PREFIX = "meeting:%s:chat";
    private static final Duration KEY_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void incrementVoiceCount(UUID meetingId, UUID userId) {
        String key = buildVoiceKey(meetingId);
        redisTemplate.opsForHash().increment(key, userId.toString(), 1);
        redisTemplate.expire(key, KEY_TTL);
    }

    @Override
    public void incrementChatCount(UUID meetingId, UUID userId) {
        String key = buildChatKey(meetingId);
        redisTemplate.opsForHash().increment(key, userId.toString(), 1);
        redisTemplate.expire(key, KEY_TTL);
    }

    @Override
    public int getVoiceCount(UUID meetingId, UUID userId) {
        String key = buildVoiceKey(meetingId);
        Object value = redisTemplate.opsForHash().get(key, userId.toString());
        return parseCount(value);
    }

    @Override
    public int getChatCount(UUID meetingId, UUID userId) {
        String key = buildChatKey(meetingId);
        Object value = redisTemplate.opsForHash().get(key, userId.toString());
        return parseCount(value);
    }

    @Override
    public Map<UUID, Integer> getAllVoiceCounts(UUID meetingId) {
        String key = buildVoiceKey(meetingId);
        return getAllCounts(key);
    }

    @Override
    public Map<UUID, Integer> getAllChatCounts(UUID meetingId) {
        String key = buildChatKey(meetingId);
        return getAllCounts(key);
    }

    @Override
    public void clearMeetingData(UUID meetingId) {
        String voiceKey = buildVoiceKey(meetingId);
        String chatKey = buildChatKey(meetingId);
        redisTemplate.delete(List.of(voiceKey, chatKey));
    }

    private Map<UUID, Integer> getAllCounts(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<UUID, Integer> result = new HashMap<>();

        entries.forEach((userIdStr, count) -> {
            UUID userId = UUID.fromString((String) userIdStr);
            int countValue = parseCount(count);
            result.put(userId, countValue);
        });

        return result;
    }

    private int parseCount(Object value) {
        if (value == null) {
            return 0;
        }
        return Integer.parseInt(value.toString());
    }

    private String buildVoiceKey(UUID meetingId) {
        return String.format(VOICE_KEY_PREFIX, meetingId);
    }

    private String buildChatKey(UUID meetingId) {
        return String.format(CHAT_KEY_PREFIX, meetingId);
    }
}
