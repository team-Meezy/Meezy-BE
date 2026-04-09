package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.port.out.RecordingFailureReporter;
import com.example.meezy.bc.sharedkernel.file.AudioStoragePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordingAsyncProcessor 테스트")
class RecordingAsyncProcessorTest {

    @Mock
    private AudioStoragePort audioStoragePort;

    @Mock
    private RecordingTransactionHandler transactionHandler;

    @Mock
    private RecordingFailureReporter recordingFailureReporter;

    @InjectMocks
    private RecordingAsyncProcessor recordingAsyncProcessor;

    @Test
    @DisplayName("저장에 성공하면 실패 리포트는 남기지 않고 임시 파일을 삭제한다")
    void process_succeeds_without_failure_reporting() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID meetingId = UUID.randomUUID();
        Path tempFile = Files.createTempFile("recording-processor-", ".mp3");
        given(audioStoragePort.uploadAudioFromPath(tempFile)).willReturn("recordings/test.mp3");

        recordingAsyncProcessor.process(teamId, meetingId, tempFile);

        verify(transactionHandler).validateMeetingOwnership(teamId, meetingId);
        verify(transactionHandler).saveRecording(teamId, meetingId, "recordings/test.mp3");
        verify(recordingFailureReporter, never()).markFailed(any(), any(), any());
        assertThat(Files.exists(tempFile)).isFalse();
    }

    @Test
    @DisplayName("DB 저장이 실패하면 실패 상태를 기록하고 S3 삭제는 하지 않는다")
    void process_marks_failed_when_save_recording_fails() throws Exception {
        UUID teamId = UUID.randomUUID();
        UUID meetingId = UUID.randomUUID();
        Path tempFile = Files.createTempFile("recording-processor-", ".mp3");
        given(audioStoragePort.uploadAudioFromPath(tempFile)).willReturn("recordings/test.mp3");
        doThrow(new RuntimeException("db fail"))
                .when(transactionHandler).saveRecording(teamId, meetingId, "recordings/test.mp3");

        recordingAsyncProcessor.process(teamId, meetingId, tempFile);

        verify(recordingFailureReporter).markFailed(meetingId, "recordings/test.mp3", "db fail");
        verify(audioStoragePort, never()).deleteAudio(any());
        assertThat(Files.exists(tempFile)).isFalse();
    }
}
