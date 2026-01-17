package com.example.meezy.bc.team.meeting.domain.event;

import java.util.UUID;

public record MeetingEndedEvent(
        UUID teamId,
        UUID meetingId
) implements MeetingEvent {
}
