package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.port.out.RecordingFailureReporter;
import com.example.meezy.bc.sharedkernel.file.AudioStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordingAsyncProcessor {

    private final AudioStoragePort audioStoragePort;
    private final RecordingTransactionHandler transactionHandler;
    private final RecordingFailureReporter recordingFailureReporter;

    @Async
    public void process(UUID teamId, UUID meetingId, Path tempFile) {
        log.info("녹음 비동기 처리 시작: teamId={}, meetingId={}, tempFile={}", teamId, meetingId, tempFile);
        try {
            transactionHandler.validateMeetingOwnership(teamId, meetingId);

            String s3Key = audioStoragePort.uploadAudioFromPath(tempFile);
            log.info("녹음 S3 업로드 완료: meetingId={}, s3Key={}", meetingId, s3Key);

            try {
                transactionHandler.saveRecording(teamId, meetingId, s3Key);
                log.info("녹음 비동기 처리 완료: meetingId={}, s3Key={}", meetingId, s3Key);
            } catch (Exception e) {
                log.error("녹음 DB 저장 실패, 실패 상태 기록: key={}", s3Key, e);
                try {
                    recordingFailureReporter.markFailed(meetingId, s3Key, extractFailureReason(e));
                } catch (Exception failureReportException) {
                    log.error("녹음 실패 상태 기록 실패: key={}", s3Key, failureReportException);
                }
            }
        } catch (Exception e) {
            log.error("녹음 비동기 처리 실패: meetingId={}", meetingId, e);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    private String extractFailureReason(Exception e) {
        Throwable rootCause = e;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage() != null ? rootCause.getMessage() : e.getClass().getSimpleName();
    }

    private void deleteTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            log.warn("임시 파일 삭제 실패: {}", tempFile, e);
        }
    }
}
