package com.example.meezy.bc.collaboration.participation_metrics.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.participation_metrics.application.port.out.ParticipationCounterPort;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordParticipationService {

    private final ParticipationCounterPort participationCounterPort;
    private final CurrentUserQuery currentUserQuery;
    private final MeetingRepository meetingRepository;

    public void recordVoice(UUID meetingId) {
        if (isInactiveMeeting(meetingId)) {
            return;
        }
        UUID userId = currentUserQuery.currentUser().userId().value();
        participationCounterPort.incrementVoiceCount(meetingId, userId);
    }

    public void recordChat(UUID meetingId) {
        if (isInactiveMeeting(meetingId)) {
            return;
        }
        UUID userId = currentUserQuery.currentUser().userId().value();
        participationCounterPort.incrementChatCount(meetingId, userId);
    }

    private boolean isInactiveMeeting(UUID meetingId) {
        return !meetingRepository.existsByMeetingIdAndStatus(MeetingId.of(meetingId), MeetingStatus.ACTIVE);
    }
}
