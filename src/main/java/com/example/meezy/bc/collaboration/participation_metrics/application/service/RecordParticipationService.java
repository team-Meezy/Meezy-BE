package com.example.meezy.bc.collaboration.participation_metrics.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.participation_metrics.application.port.out.ParticipationCounterPort;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.exception.ParticipationSenderNotInMeetingException;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordParticipationService {

    private final ParticipationCounterPort participationCounterPort;
    private final CurrentUserQuery currentUserQuery;
    private final MeetingRepository meetingRepository;

    public void recordVoice(UUID meetingId) {
        UUID userId = resolveCurrentUserId(meetingId, "voice");
        if (!validateActiveParticipant(meetingId, userId)) {
            return;
        }
        participationCounterPort.incrementVoiceCount(meetingId, userId);
    }

    public void recordChat(UUID meetingId) {
        UUID userId = resolveCurrentUserId(meetingId, "chat");
        if (!validateActiveParticipant(meetingId, userId)) {
            return;
        }
        participationCounterPort.incrementChatCount(meetingId, userId);
    }

    /**
     * @return true if user is an active participant, false if meeting is inactive/not found
     * @throws ParticipationSenderNotInMeetingException if meeting is active but user is not a participant
     */
    private boolean validateActiveParticipant(UUID meetingId, UUID userId) {
        Meeting meeting = meetingRepository.findByMeetingIdWithParticipants(meetingId)
                .filter(Meeting::isActive)
                .orElse(null);

        if (meeting == null) {
            log.warn("Dropping participation event because meeting is inactive or missing: meetingId={}, userId={}",
                    meetingId, userId);
            participationCounterPort.evictCachedParticipantIds(meetingId);
            return false;
        }

        Optional<Set<UUID>> cached = participationCounterPort.getCachedParticipantIds(meetingId);
        if (cached.isPresent() && cached.get().contains(userId)) {
            log.debug("Validated participation sender from cache: meetingId={}, userId={}, cachedParticipantCount={}",
                    meetingId, userId, cached.get().size());
            return true;
        }

        List<UUID> activeParticipantIds = meeting.getActiveParticipantUserIds();
        Set<UUID> activeParticipantIdSet = Set.copyOf(activeParticipantIds);

        if (cached.isPresent()) {
            log.warn("Participation cache did not contain sender, refreshing from DB: meetingId={}, userId={}, cachedParticipantCount={}, activeParticipantCount={}",
                    meetingId, userId, cached.get().size(), activeParticipantIdSet.size());
        } else {
            log.info("Participation cache miss, loading active participants from DB: meetingId={}, userId={}, activeParticipantCount={}",
                    meetingId, userId, activeParticipantIdSet.size());
        }

        participationCounterPort.cacheParticipantIds(meetingId, activeParticipantIdSet);

        if (!activeParticipantIdSet.contains(userId)) {
            log.warn("Rejecting participation event because sender is not an active participant: meetingId={}, userId={}, activeParticipantIds={}",
                    meetingId, userId, activeParticipantIds);
            throw new ParticipationSenderNotInMeetingException();
        }

        log.debug("Validated participation sender from DB refresh: meetingId={}, userId={}", meetingId, userId);
        return true;
    }

    private UUID resolveCurrentUserId(UUID meetingId, String metricType) {
        try {
            UUID userId = currentUserQuery.currentUser().userId().value();
            log.debug("Resolved authenticated user for participation event: metricType={}, meetingId={}, userId={}",
                    metricType, meetingId, userId);
            return userId;
        } catch (RuntimeException e) {
            log.error("Failed to resolve authenticated user for participation event: metricType={}, meetingId={}",
                    metricType, meetingId, e);
            throw e;
        }
    }
}
