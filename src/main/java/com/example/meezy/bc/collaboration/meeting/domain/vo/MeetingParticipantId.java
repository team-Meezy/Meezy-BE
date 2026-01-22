package com.example.meezy.bc.collaboration.meeting.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record MeetingParticipantId(
        UUID value
) {

    public static MeetingParticipantId newId() {
        return new MeetingParticipantId(UUID.randomUUID());
    }

    public static MeetingParticipantId of(UUID participantId) {
        return new MeetingParticipantId(participantId);
    }
}
