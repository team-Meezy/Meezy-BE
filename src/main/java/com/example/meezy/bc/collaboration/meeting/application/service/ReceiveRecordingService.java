package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.service.exception.EmptyRecordingException;
import com.example.meezy.bc.collaboration.meeting.application.service.exception.InvalidRecordingFormatException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.NotTeamMemberException;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.domain.vo.UserId;
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
    private final CurrentUserQuery currentUserQuery;
    private final TeamRepository teamRepository;

    public void receive(UUID teamId, UUID meetingId, MultipartFile recording) {
        log.info(
                "녹음 업로드 요청 수신: teamId={}, meetingId={}, filename={}, size={}, contentType={}",
                teamId,
                meetingId,
                recording != null ? recording.getOriginalFilename() : null,
                recording != null ? recording.getSize() : null,
                recording != null ? recording.getContentType() : null
        );
        // ① 팀 멤버 검증
        UserId currentUserId = currentUserQuery.currentUser().userId();
        validateTeamMembership(teamId, currentUserId);

        // ② 파일 형식 검증만 (DB 접근 없음)
        validateRecording(recording);

        // ② Spring multipart 임시파일 경로 직접 사용 (복사 없음)
        Path tempFile = extractTempFilePath(recording);

        // ③ 비동기: 팀 검증 + S3 업로드 + DB 저장
        asyncProcessor.process(teamId, meetingId, tempFile);
        log.info("녹음 업로드 비동기 처리 위임 완료: teamId={}, meetingId={}", teamId, meetingId);
    }

    private void validateTeamMembership(UUID teamId, UserId userId) {
        if (!teamRepository.existsMemberByTeamIdAndUserId(teamId, userId)) {
            throw new NotTeamMemberException();
        }
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
