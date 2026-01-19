package com.example.meezy.bc.team.meeting.application.service;

import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.team.meeting.domain.Meeting;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.team.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.team.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import com.example.meezy.bc.user.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("JoinMeetingService 테스트")
class JoinMeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private JoinMeetingService joinMeetingService;

    private UUID teamIdValue;
    private TeamId teamId;
    private UserId hostUserId;
    private UserId newUserId;
    private AuthenticatedUser authenticatedUser;
    private Meeting meeting;

    @BeforeEach
    void setUp() {
        teamIdValue = UUID.randomUUID();
        teamId = TeamId.of(teamIdValue);
        hostUserId = UserId.newId();
        newUserId = UserId.newId();
        authenticatedUser = AuthenticatedUser.builder()
                .userId(newUserId)
                .accountId("newUser")
                .name("New User")
                .build();
        meeting = Meeting.start(teamId, hostUserId);
    }

    @Test
    @DisplayName("진행 중인 회의에 참가할 수 있다")
    void join_adds_participant() {
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.of(meeting));
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);

        MeetingResponse response = joinMeetingService.join(teamIdValue);

        assertThat(response).isNotNull();
        assertThat(meeting.getActiveParticipantCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("진행 중인 회의가 없으면 참가할 수 없다")
    void join_throws_when_no_active_meeting() {
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> joinMeetingService.join(teamIdValue))
                .isInstanceOf(MeetingNotFoundException.class);
    }
}
