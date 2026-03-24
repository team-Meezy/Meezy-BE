package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.service.exception.EmptyRecordingException;
import com.example.meezy.bc.collaboration.meeting.application.service.exception.InvalidRecordingFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveRecordingService {

    private final RecordingAsyncProcessor asyncProcessor;

    public void receive(UUID teamId, UUID meetingId, MultipartFile recording) {
        // ① 파일 형식 검증만 (DB 접근 없음)
        validateRecording(recording);

        // ② Spring multipart 임시파일 경로 직접 사용 (복사 없음)
        Path tempFile = extractTempFilePath(recording);

        // ③ 비동기: 팀 검증 + S3 업로드 + DB 저장
        asyncProcessor.process(teamId, meetingId, tempFile);
    }

    private Path extractTempFilePath(MultipartFile recording) {
        try {
            File tempFile = File.createTempFile("recording-", ".mp3");
            recording.transferTo(tempFile);
            return tempFile.toPath();
        } catch (Exception e) {
            throw new RuntimeException("임시 파일 저장 실패", e);
        }
    }

    private void validateRecording(MultipartFile recording) {
        if (recording == null || recording.isEmpty()) {
            throw new EmptyRecordingException();
        }

        String filename = recording.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".mp3")) {
            throw new InvalidRecordingFormatException();
        }

        String contentType = recording.getContentType();
        if (contentType == null || !contentType.equals("audio/mpeg")) {
            throw new InvalidRecordingFormatException();
        }
    }
}
