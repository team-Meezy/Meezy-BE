package com.example.meezy.bc.collaboration.meeting.domain.event;

import java.util.UUID;

public record RecordingReceivedEvent(
        UUID meetingId,
        String s3Key,
        String title
) {
}
