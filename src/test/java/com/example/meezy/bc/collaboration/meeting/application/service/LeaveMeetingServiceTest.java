package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.LeaveResponse;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.user.user.domain.vo.UserId;
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
@DisplayName("LeaveMeetingService 테스트")
class LeaveMeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LeaveMeetingService leaveMeetingService;

    private UUID teamIdValue;
    private TeamId teamId;
    private UserId hostUserId;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        teamIdValue = UUID.randomUUID();
        teamId = TeamId.of(teamIdValue);
        hostUserId = UserId.newId();
        authenticatedUser = AuthenticatedUser.builder()
                .userId(hostUserId)
                .accountId("hostUser")
                .name("Host User")
                .build();
    }

    @Test
    @DisplayName("회의에서 퇴장할 수 있다")
    void leave_removes_participant() {
        Meeting meeting = Meeting.start(teamId, hostUserId);
        UserId anotherUserId = UserId.newId();
        meeting.join(anotherUserId);
        meeting.pullDomainEvents();

        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.of(meeting));
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);

        LeaveResponse response = leaveMeetingService.leave(teamIdValue);

        assertThat(response.isMeetingActive()).isTrue();
        assertThat(meeting.getActiveParticipantCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("마지막 참가자가 퇴장하면 회의가 종료된다")
    void leave_ends_meeting_when_last_participant() {
        Meeting meeting = Meeting.start(teamId, hostUserId);
        meeting.pullDomainEvents();

        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.of(meeting));
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);

        LeaveResponse response = leaveMeetingService.leave(teamIdValue);

        assertThat(response.isMeetingActive()).isFalse();
        assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.ENDED);
    }

    @Test
    @DisplayName("진행 중인 회의가 없으면 퇴장할 수 없다")
    void leave_throws_when_no_active_meeting() {
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> leaveMeetingService.leave(teamIdValue))
                .isInstanceOf(MeetingNotFoundException.class);
    }
}
