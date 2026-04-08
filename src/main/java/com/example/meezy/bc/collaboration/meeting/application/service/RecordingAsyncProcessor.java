package com.example.meezy.bc.collaboration.meeting.application.service;

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

    @Async
    public void process(UUID teamId, UUID meetingId, Path tempFile) {
        log.info("녹음 비동기 처리 시작: teamId={}, meetingId={}, tempFile={}", teamId, meetingId, tempFile);
        try {
            // ① 팀/회의 검증 (비동기 스레드에서)
            transactionHandler.validateMeetingOwnership(teamId, meetingId);

            // ② S3 업로드
            String s3Key = audioStoragePort.uploadAudioFromPath(tempFile);
            log.info("녹음 S3 업로드 완료: meetingId={}, s3Key={}", meetingId, s3Key);

            try {
                // ③ DB 저장 + 이벤트 발행
                transactionHandler.saveRecording(teamId, meetingId, s3Key);
                log.info("녹음 비동기 처리 완료: meetingId={}, s3Key={}", meetingId, s3Key);
            } catch (Exception e) {
                log.error("녹음 DB 저장 실패, S3 파일 삭제: key={}", s3Key, e);
                try {
                    audioStoragePort.deleteAudio(s3Key);
                } catch (Exception deleteEx) {
                    log.error("S3 보상 삭제 실패 (고아 파일 발생): key={}", s3Key, deleteEx);
                }
            }
        } catch (Exception e) {
            log.error("녹음 비동기 처리 실패: meetingId={}", meetingId, e);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    private void deleteTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            log.warn("임시 파일 삭제 실패: {}", tempFile, e);
        }
    }
}
