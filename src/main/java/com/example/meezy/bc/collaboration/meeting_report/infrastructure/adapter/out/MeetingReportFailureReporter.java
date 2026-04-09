package com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out;

import com.example.meezy.bc.collaboration.meeting.application.port.out.RecordingFailureReporter;
import com.example.meezy.bc.collaboration.meeting_report.application.service.internal.ReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MeetingReportFailureReporter implements RecordingFailureReporter {

    private final ReportGenerator reportGenerator;

    @Override
    public void markFailed(UUID meetingId, String sourceAudioKey, String reason) {
        reportGenerator.markFailed(meetingId, sourceAudioKey, reason);
    }
}
