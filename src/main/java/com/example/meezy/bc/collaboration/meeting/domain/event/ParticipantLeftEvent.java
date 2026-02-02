package com.example.meezy.bc.collaboration.meeting.domain.event;

import java.util.UUID;

public record ParticipantLeftEvent(
        UUID teamId,
        UUID meetingId,
        UUID leftUserId
) implements MeetingEvent {
}
