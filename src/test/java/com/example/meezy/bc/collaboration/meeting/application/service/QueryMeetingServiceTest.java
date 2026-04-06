package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.collaboration.meeting.application.port.out.IceServerQueryPort;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.exception.NotTeamMemberException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryMeetingService 테스트")
class QueryMeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IceServerQueryPort iceServerQueryPort;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @InjectMocks
    private QueryMeetingService queryMeetingService;

    private UUID teamIdValue;
    private UserId userId;
    private AuthenticatedUser authenticatedUser;
    private Meeting meeting;

    @BeforeEach
    void setUp() {
        teamIdValue = UUID.randomUUID();
        TeamId teamId = TeamId.of(teamIdValue);
        userId = UserId.newId();
        authenticatedUser = AuthenticatedUser.builder()
                .userId(userId)
                .accountId("user")
                .name("User")
                .build();
        meeting = Meeting.start(teamId, userId);
    }

    @Test
    @DisplayName("진행 중인 회의를 조회할 수 있다")
    void findActiveMeeting_returns_meeting() {
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                .willReturn(true);
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.of(meeting));
        given(userRepository.findByUserId_ValueIn(any())).willReturn(List.of());
        given(iceServerQueryPort.getIceServers()).willReturn(List.of());

        Optional<MeetingResponse> result = queryMeetingService.findActiveMeeting(teamIdValue);

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("진행 중인 회의가 없으면 빈 Optional을 반환한다")
    void findActiveMeeting_returns_empty_when_no_active_meeting() {
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                .willReturn(true);
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.empty());
        given(iceServerQueryPort.getIceServers()).willReturn(List.of());

        Optional<MeetingResponse> result = queryMeetingService.findActiveMeeting(teamIdValue);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("팀 멤버가 아니면 회의 조회 시 예외가 발생한다")
    void findActiveMeeting_throws_when_not_team_member() {
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                .willReturn(false);

        assertThatThrownBy(() -> queryMeetingService.findActiveMeeting(teamIdValue))
                .isInstanceOf(NotTeamMemberException.class);
    }
}
