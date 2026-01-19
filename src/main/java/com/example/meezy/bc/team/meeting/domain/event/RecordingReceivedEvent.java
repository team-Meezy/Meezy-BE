package com.example.meezy.bc.team.meeting.domain.event;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record RecordingReceivedEvent(
        UUID meetingId,
        MultipartFile audio
) {
}
