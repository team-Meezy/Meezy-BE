package com.example.meezy.bc.collaboration.participation_metrics.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.participation_metrics.application.port.out.ParticipationCounterPort;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.exception.ParticipationSenderNotInMeetingException;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordParticipationService {

    private final ParticipationCounterPort participationCounterPort;
    private final CurrentUserQuery currentUserQuery;
    private final MeetingRepository meetingRepository;

    public void recordVoice(UUID meetingId) {
        UUID userId = currentUserQuery.currentUser().userId().value();
        if (!validateActiveParticipant(meetingId, userId)) {
            return;
        }
        participationCounterPort.incrementVoiceCount(meetingId, userId);
    }

    public void recordChat(UUID meetingId) {
        UUID userId = currentUserQuery.currentUser().userId().value();
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
        Optional<Set<UUID>> cached = participationCounterPort.getCachedParticipantIds(meetingId);
        if (cached.isPresent()) {
            if (!cached.get().contains(userId)) {
                throw new ParticipationSenderNotInMeetingException();
            }
            return true;
        }

        Meeting meeting = meetingRepository.findByMeetingId_Value(meetingId)
                .filter(Meeting::isActive)
                .orElse(null);

        if (meeting == null) {
            return false;
        }

        List<UUID> activeParticipantIds = meeting.getActiveParticipantUserIds();
        participationCounterPort.cacheParticipantIds(meetingId, Set.copyOf(activeParticipantIds));

        if (!activeParticipantIds.contains(userId)) {
            throw new ParticipationSenderNotInMeetingException();
        }
        return true;
    }
}
