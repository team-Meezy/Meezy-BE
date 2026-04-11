package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.NotTeamMemberException;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReceiveRecordingService tests")
class ReceiveRecordingServiceTest {

    @Mock
    private RecordingAsyncProcessor asyncProcessor;

    @Mock
    private RecordingTransactionHandler transactionHandler;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private ReceiveRecordingService receiveRecordingService;

    private UUID teamId;
    private UUID meetingId;
    private UserId userId;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        teamId = UUID.randomUUID();
        meetingId = UUID.randomUUID();
        userId = UserId.newId();
        authenticatedUser = AuthenticatedUser.builder()
                .userId(userId)
                .accountId("recording-user")
                .name("Recording User")
                .build();
    }

    @Test
    @DisplayName("validates meeting ownership before delegating async processing")
    void receive_validates_meeting_before_async_processing() throws Exception {
        MockMultipartFile recording = new MockMultipartFile(
                "file",
                "recording.mp3",
                "audio/mpeg",
                "test-audio".getBytes()
        );
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(teamId, userId)).willReturn(true);

        receiveRecordingService.receive(teamId, meetingId, recording, "Sprint Review");

        verify(transactionHandler).validateMeetingOwnership(teamId, meetingId);

        ArgumentCaptor<Path> pathCaptor = ArgumentCaptor.forClass(Path.class);
        verify(asyncProcessor).process(eq(teamId), eq(meetingId), eq("Sprint Review"), pathCaptor.capture());
        assertThat(Files.exists(pathCaptor.getValue())).isTrue();
        Files.deleteIfExists(pathCaptor.getValue());
    }

    @Test
    @DisplayName("rejects uploads from non-team members")
    void receive_throws_when_not_team_member() {
        MockMultipartFile recording = new MockMultipartFile(
                "file",
                "recording.mp3",
                "audio/mpeg",
                "test-audio".getBytes()
        );
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(teamId, userId)).willReturn(false);

        assertThatThrownBy(() -> receiveRecordingService.receive(teamId, meetingId, recording, "Sprint Review"))
                .isInstanceOf(NotTeamMemberException.class);

        verify(transactionHandler, never()).validateMeetingOwnership(any(), any());
        verify(asyncProcessor, never()).process(any(), any(), any(), any());
    }

    @Test
    @DisplayName("does not start async processing when meeting validation fails")
    void receive_throws_when_meeting_validation_fails() {
        MockMultipartFile recording = new MockMultipartFile(
                "file",
                "recording.mp3",
                "audio/mpeg",
                "test-audio".getBytes()
        );
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(teamId, userId)).willReturn(true);
        doThrow(new MeetingNotFoundException()).when(transactionHandler).validateMeetingOwnership(teamId, meetingId);

        assertThatThrownBy(() -> receiveRecordingService.receive(teamId, meetingId, recording, "Sprint Review"))
                .isInstanceOf(MeetingNotFoundException.class);

        verify(asyncProcessor, never()).process(any(), any(), any(), any());
    }
}
