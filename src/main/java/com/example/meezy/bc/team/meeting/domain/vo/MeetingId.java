package com.example.meezy.bc.team.meeting.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record MeetingId(
        UUID value
) {

    public static MeetingId newId() {
        return new MeetingId(UUID.randomUUID());
    }

    public static MeetingId of(UUID meetingId) {
        return new MeetingId(meetingId);
    }
}
