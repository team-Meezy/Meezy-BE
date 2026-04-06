package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.port.out.SignalMessagePublisher;
import com.example.meezy.bc.collaboration.meeting.application.port.out.SignalRateLimitPort;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.request.SignalMessage;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.SignalRecipientNotInMeetingException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.SignalSenderMismatchException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.SignalSenderNotInMeetingException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RelaySignalService 테스트")
class RelaySignalServiceTest {

    @Mock
    private SignalMessagePublisher signalMessagePublisher;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private SignalRateLimitPort signalRateLimitPort;

    @InjectMocks
    private RelaySignalService relaySignalService;

    private UUID teamIdValue;
    private UserId senderUserId;
    private UserId recipientUserId;
    private AuthenticatedUser authenticatedUser;
    private Meeting meeting;

    @BeforeEach
    void setUp() {
        teamIdValue = UUID.randomUUID();
        senderUserId = UserId.newId();
        recipientUserId = UserId.newId();
        authenticatedUser = AuthenticatedUser.builder()
                .userId(senderUserId)
                .accountId("sender")
                .name("Sender")
                .build();

        meeting = Meeting.start(TeamId.of(teamIdValue), senderUserId);
        meeting.join(recipientUserId);
    }

    @Test
    @DisplayName("참가자가 다른 참가자에게 시그널을 보낼 수 있다")
    void relay_succeeds_for_active_participants() {
        SignalMessage message = new SignalMessage.Offer(
                senderUserId.value(), recipientUserId.value(), "sdp-data"
        );

        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                .willReturn(true);
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.of(meeting));

        relaySignalService.relay(teamIdValue, message);

        verify(signalMessagePublisher).publish(teamIdValue, message);
    }

    @Test
    @DisplayName("미팅에 참가하지 않은 팀원은 시그널을 보낼 수 없다")
    void relay_throws_when_sender_not_in_meeting() {
        UserId nonParticipantUserId = UserId.newId();
        AuthenticatedUser nonParticipant = AuthenticatedUser.builder()
                .userId(nonParticipantUserId)
                .accountId("outsider")
                .name("Outsider")
                .build();

        SignalMessage message = new SignalMessage.Offer(
                nonParticipantUserId.value(), recipientUserId.value(), "sdp-data"
        );

        given(currentUserQuery.currentUser()).willReturn(nonParticipant);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                .willReturn(true);
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.of(meeting));

        assertThatThrownBy(() -> relaySignalService.relay(teamIdValue, message))
                .isInstanceOf(SignalSenderNotInMeetingException.class);
    }

    @Test
    @DisplayName("이미 퇴장한 사용자는 시그널을 보낼 수 없다")
    void relay_throws_when_sender_already_left() {
        meeting.leave(senderUserId);

        SignalMessage message = new SignalMessage.Offer(
                senderUserId.value(), recipientUserId.value(), "sdp-data"
        );

        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                .willReturn(true);
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.of(meeting));

        assertThatThrownBy(() -> relaySignalService.relay(teamIdValue, message))
                .isInstanceOf(SignalSenderNotInMeetingException.class);
    }

    @Test
    @DisplayName("수신자가 미팅 참가자가 아니면 시그널을 보낼 수 없다")
    void relay_throws_when_recipient_not_in_meeting() {
        UUID unknownUserId = UUID.randomUUID();
        SignalMessage message = new SignalMessage.Offer(
                senderUserId.value(), unknownUserId, "sdp-data"
        );

        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                .willReturn(true);
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.of(meeting));

        assertThatThrownBy(() -> relaySignalService.relay(teamIdValue, message))
                .isInstanceOf(SignalRecipientNotInMeetingException.class);
    }

    @Test
    @DisplayName("fromUserId와 인증 사용자가 다르면 실패한다")
    void relay_throws_when_sender_identity_mismatch() {
        UUID fakeUserId = UUID.randomUUID();
        SignalMessage message = new SignalMessage.Offer(
                fakeUserId, recipientUserId.value(), "sdp-data"
        );

        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);

        assertThatThrownBy(() -> relaySignalService.relay(teamIdValue, message))
                .isInstanceOf(SignalSenderMismatchException.class);
    }

    @Test
    @DisplayName("활성 회의가 없으면 시그널을 보낼 수 없다")
    void relay_throws_when_no_active_meeting() {
        SignalMessage message = new SignalMessage.Offer(
                senderUserId.value(), recipientUserId.value(), "sdp-data"
        );

        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                .willReturn(true);
        given(meetingRepository.findByTeamIdAndStatus(any(TeamId.class), eq(MeetingStatus.ACTIVE)))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> relaySignalService.relay(teamIdValue, message))
                .isInstanceOf(MeetingNotFoundException.class);
    }
}
