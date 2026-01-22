package com.example.meezy.bc.collaboration.meeting.domain.event;

import java.util.UUID;

public record RecordingReceivedEvent(
        UUID meetingId,
        byte[] audioData,
        String originalFilename,
        String contentType
) {
}
