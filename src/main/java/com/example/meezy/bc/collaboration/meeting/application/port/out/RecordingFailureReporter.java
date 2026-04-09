package com.example.meezy.bc.collaboration.meeting.application.port.out;

import java.util.UUID;

public interface RecordingFailureReporter {

    void markFailed(UUID meetingId, String sourceAudioKey, String reason);
}
