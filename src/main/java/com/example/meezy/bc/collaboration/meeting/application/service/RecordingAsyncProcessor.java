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
    public void process(UUID teamId, UUID meetingId, String title, Path tempFile) {
        log.info("?뱀쓬 鍮꾨룞湲?泥섎━ ?쒖옉: teamId={}, meetingId={}, title={}, tempFile={}", teamId, meetingId, title, tempFile);
        try {
            transactionHandler.validateMeetingOwnership(teamId, meetingId);

            String s3Key = audioStoragePort.uploadAudioFromPath(tempFile);
            log.info("?뱀쓬 S3 ?낅줈???꾨즺: meetingId={}, s3Key={}", meetingId, s3Key);

            try {
                transactionHandler.saveRecording(teamId, meetingId, s3Key, title);
                log.info("?뱀쓬 鍮꾨룞湲?泥섎━ ?꾨즺: meetingId={}, s3Key={}", meetingId, s3Key);
            } catch (Exception e) {
                log.error("?뱀쓬 DB ????ㅽ뙣, ?ㅽ뙣 ?곹깭 湲곕줉: key={}", s3Key, e);
                try {
                    recordingFailureReporter.markFailed(meetingId, s3Key, title, extractFailureReason(e));
                } catch (Exception failureReportException) {
                    log.error("?뱀쓬 ?ㅽ뙣 ?곹깭 湲곕줉 ?ㅽ뙣: key={}", s3Key, failureReportException);
                }
            }
        } catch (Exception e) {
            log.error("?뱀쓬 鍮꾨룞湲?泥섎━ ?ㅽ뙣: meetingId={}", meetingId, e);
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
            log.warn("?꾩떆 ?뚯씪 ??젣 ?ㅽ뙣: {}", tempFile, e);
        }
    }
}
