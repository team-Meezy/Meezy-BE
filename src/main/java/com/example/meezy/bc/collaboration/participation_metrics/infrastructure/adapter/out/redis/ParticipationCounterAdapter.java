package com.example.meezy.bc.collaboration.participation_metrics.infrastructure.adapter.out.redis;

import com.example.meezy.bc.collaboration.participation_metrics.application.port.out.ParticipationCounterPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParticipationCounterAdapter implements ParticipationCounterPort {

    private static final String VOICE_KEY_PREFIX = "meeting:%s:voice";
    private static final String CHAT_KEY_PREFIX = "meeting:%s:chat";
    private static final String PARTICIPANTS_KEY_PREFIX = "meeting:%s:participants";
    private static final Duration KEY_TTL = Duration.ofHours(24);
    private static final Duration PARTICIPANTS_CACHE_TTL = Duration.ofSeconds(30);

    private static final DefaultRedisScript<Long> HASH_INCREMENT_SCRIPT;
    private static final DefaultRedisScript<Long> SET_ADD_WITH_EXPIRE_SCRIPT;

    static {
        HASH_INCREMENT_SCRIPT = new DefaultRedisScript<>();
        HASH_INCREMENT_SCRIPT.setScriptText(
                "local count = redis.call('HINCRBY', KEYS[1], ARGV[1], 1) " +
                "if redis.call('TTL', KEYS[1]) == -1 then redis.call('EXPIRE', KEYS[1], ARGV[2]) end " +
                "return count"
        );
        HASH_INCREMENT_SCRIPT.setResultType(Long.class);

        SET_ADD_WITH_EXPIRE_SCRIPT = new DefaultRedisScript<>();
        SET_ADD_WITH_EXPIRE_SCRIPT.setScriptText(
                "for i = 2, #ARGV do redis.call('SADD', KEYS[1], ARGV[i]) end " +
                "redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                "return 1"
        );
        SET_ADD_WITH_EXPIRE_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate redisTemplate;

    @Override
    public void incrementVoiceCount(UUID meetingId, UUID userId) {
        String key = buildVoiceKey(meetingId);
        Long newCount = redisTemplate.execute(
                HASH_INCREMENT_SCRIPT,
                List.of(key),
                userId.toString(),
                String.valueOf(KEY_TTL.getSeconds())
        );
        log.debug("Incremented voice participation counter: meetingId={}, userId={}, key={}, newCount={}",
                meetingId, userId, key, newCount);
    }

    @Override
    public void incrementChatCount(UUID meetingId, UUID userId) {
        String key = buildChatKey(meetingId);
        Long newCount = redisTemplate.execute(
                HASH_INCREMENT_SCRIPT,
                List.of(key),
                userId.toString(),
                String.valueOf(KEY_TTL.getSeconds())
        );
        log.debug("Incremented chat participation counter: meetingId={}, userId={}, key={}, newCount={}",
                meetingId, userId, key, newCount);
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
        Map<UUID, Integer> counts = getAllCounts(key);
        log.debug("Loaded all voice participation counters: meetingId={}, key={}, entryCount={}, counts={}",
                meetingId, key, counts.size(), counts);
        return counts;
    }

    @Override
    public Map<UUID, Integer> getAllChatCounts(UUID meetingId) {
        String key = buildChatKey(meetingId);
        Map<UUID, Integer> counts = getAllCounts(key);
        log.debug("Loaded all chat participation counters: meetingId={}, key={}, entryCount={}, counts={}",
                meetingId, key, counts.size(), counts);
        return counts;
    }

    @Override
    public void clearMeetingData(UUID meetingId) {
        String voiceKey = buildVoiceKey(meetingId);
        String chatKey = buildChatKey(meetingId);
        String participantsKey = buildParticipantsKey(meetingId);
        redisTemplate.delete(List.of(voiceKey, chatKey, participantsKey));
        log.info("Cleared participation Redis keys: meetingId={}, keys={}",
                meetingId, List.of(voiceKey, chatKey, participantsKey));
    }

    @Override
    public Optional<Set<UUID>> getCachedParticipantIds(UUID meetingId) {
        String key = buildParticipantsKey(meetingId);
        Set<String> members = redisTemplate.opsForSet().members(key);
        if (members == null || members.isEmpty()) {
            log.debug("Participation participant cache miss: meetingId={}, key={}", meetingId, key);
            return Optional.empty();
        }
        Set<UUID> participantIds = members.stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
        log.debug("Participation participant cache hit: meetingId={}, key={}, participantCount={}, participantIds={}",
                meetingId, key, participantIds.size(), participantIds);
        return Optional.of(participantIds);
    }

    @Override
    public void cacheParticipantIds(UUID meetingId, Set<UUID> participantIds) {
        if (participantIds.isEmpty()) {
            log.debug("Skipping participation participant cache store because active participant set is empty: meetingId={}",
                    meetingId);
            return;
        }
        String key = buildParticipantsKey(meetingId);
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(PARTICIPANTS_CACHE_TTL.getSeconds()));
        participantIds.stream().map(UUID::toString).forEach(args::add);
        redisTemplate.execute(
                SET_ADD_WITH_EXPIRE_SCRIPT,
                List.of(key),
                (Object[]) args.toArray(String[]::new)
        );
        log.debug("Stored participation participant cache: meetingId={}, key={}, participantCount={}, ttlSeconds={}",
                meetingId, key, participantIds.size(), PARTICIPANTS_CACHE_TTL.getSeconds());
    }

    @Override
    public void evictCachedParticipantIds(UUID meetingId) {
        String key = buildParticipantsKey(meetingId);
        redisTemplate.delete(key);
        log.debug("Evicted participation participant cache: meetingId={}, key={}", meetingId, key);
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

    private String buildParticipantsKey(UUID meetingId) {
        return String.format(PARTICIPANTS_KEY_PREFIX, meetingId);
    }
}
