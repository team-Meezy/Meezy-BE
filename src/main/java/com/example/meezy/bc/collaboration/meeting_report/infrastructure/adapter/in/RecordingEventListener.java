package com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.in;

import com.example.meezy.bc.collaboration.meeting.domain.event.RecordingReceivedEvent;
import com.example.meezy.bc.collaboration.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.collaboration.meeting_report.application.service.internal.ReportGenerator;
import com.example.meezy.bc.sharedkernel.file.AudioStoragePort;
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
    private final AudioStoragePort audioStoragePort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRecordingReceived(RecordingReceivedEvent event) {
        log.info("회의 리포트 생성 시작: meetingId={}, s3Key={}", event.meetingId(), event.s3Key());
        try {
            String transcript = meetingAnalyzerPort.transcribe(event.s3Key());

            reportGenerator.generate(event.meetingId(), transcript);

            audioStoragePort.deleteAudio(event.s3Key());
            log.info("회의 리포트 생성 완료: meetingId={}", event.meetingId());
        } catch (Exception e) {
            log.error("회의 리포트 생성 실패 (S3 오디오 유지됨): meetingId={}, s3Key={}", event.meetingId(), event.s3Key(), e);
        }
    }
}
