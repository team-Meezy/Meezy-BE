package com.example.meezy.bc.collaboration.participation_metrics.application.port.out;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ParticipationCounterPort {

    void incrementVoiceCount(UUID meetingId, UUID userId);

    void incrementChatCount(UUID meetingId, UUID userId);

    int getVoiceCount(UUID meetingId, UUID userId);

    int getChatCount(UUID meetingId, UUID userId);

    Map<UUID, Integer> getAllVoiceCounts(UUID meetingId);

    Map<UUID, Integer> getAllChatCounts(UUID meetingId);

    void clearMeetingData(UUID meetingId);

    Optional<Set<UUID>> getCachedParticipantIds(UUID meetingId);

    void cacheParticipantIds(UUID meetingId, Set<UUID> participantIds);
}
