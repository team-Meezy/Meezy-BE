package com.example.meezy.bc.team.meeting_report.infrastructure.adapter.in;

import com.example.meezy.bc.team.meeting.domain.event.RecordingReceivedEvent;
import com.example.meezy.bc.team.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.team.meeting_report.application.service.internal.ReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordingEventListener {

    private final MeetingAnalyzerPort meetingAnalyzerPort;
    private final ReportGenerator reportGenerator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRecordingReceived(RecordingReceivedEvent event) {
        try {
            String transcript = meetingAnalyzerPort.transcribe(event.audio());

            reportGenerator.generate(event.meetingId(), transcript);

        } catch (Exception e) {
            log.error("회의 리포트 생성 실패: meetingId={}", event.meetingId(), e);
        }
    }
}
