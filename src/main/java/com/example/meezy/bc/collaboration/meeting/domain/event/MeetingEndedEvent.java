package com.example.meezy.bc.collaboration.meeting.domain.event;

import java.util.UUID;

public record MeetingEndedEvent(
        UUID teamId,
        UUID meetingId
) implements MeetingEvent {
}
