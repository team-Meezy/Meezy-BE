package com.example.meezy.bc.collaboration.meeting.domain.event;

import java.util.List;
import java.util.UUID;

public record ParticipantJoinedEvent(
        UUID teamId,
        UUID meetingId,
        UUID joinedUserId,
        List<UUID> existingParticipantIds
) implements MeetingEvent {
}
