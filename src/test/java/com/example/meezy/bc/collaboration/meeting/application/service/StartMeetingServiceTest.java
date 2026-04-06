package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingAlreadyExistsException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.collaboration.meeting.application.port.out.IceServerQueryPort;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StartMeetingService 테스트")
class StartMeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IceServerQueryPort iceServerQueryPort;

    @Mock
    private Team team;

    @InjectMocks
    private StartMeetingService startMeetingService;

    private UUID teamIdValue;
    private TeamId teamId;
    private UserId userId;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        teamIdValue = UUID.randomUUID();
        teamId = TeamId.of(teamIdValue);
        userId = UserId.newId();
        authenticatedUser = AuthenticatedUser.builder()
                .userId(userId)
                .accountId("testUser")
                .name("Test User")
                .build();
    }

    @Test
    @DisplayName("회의를 시작할 수 있다")
    void start_creates_meeting() {
        given(teamRepository.findByTeamIdForUpdate(teamIdValue)).willReturn(Optional.of(team));
        given(team.getTeamId()).willReturn(teamId);
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(meetingRepository.existsByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(false);
        given(userRepository.findByUserId_ValueIn(any())).willReturn(List.of());
        given(iceServerQueryPort.getIceServers()).willReturn(List.of());

        MeetingResponse response = startMeetingService.start(teamIdValue);

        assertThat(response).isNotNull();
        assertThat(response.teamId()).isEqualTo(teamIdValue);
        assertThat(response.hostUserId()).isEqualTo(userId.value());
        verify(meetingRepository).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 팀에서는 회의를 시작할 수 없다")
    void start_throws_when_team_not_found() {
        given(teamRepository.findByTeamIdForUpdate(teamIdValue)).willReturn(Optional.empty());

        assertThatThrownBy(() -> startMeetingService.start(teamIdValue))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    @DisplayName("이미 진행 중인 회의가 있으면 새 회의를 시작할 수 없다")
    void start_throws_when_meeting_already_exists() {
        given(teamRepository.findByTeamIdForUpdate(teamIdValue)).willReturn(Optional.of(team));
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(meetingRepository.existsByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(true);

        assertThatThrownBy(() -> startMeetingService.start(teamIdValue))
                .isInstanceOf(MeetingAlreadyExistsException.class);
    }
}
