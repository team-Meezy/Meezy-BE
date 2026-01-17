package com.example.meezy.bc.team.meeting.domain.event;

import java.util.UUID;

public sealed interface MeetingEvent permits
        ParticipantJoinedEvent,
        ParticipantLeftEvent,
        MeetingEndedEvent {

    UUID teamId();
    UUID meetingId();
}
