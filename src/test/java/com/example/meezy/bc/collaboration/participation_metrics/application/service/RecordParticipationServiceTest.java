package com.example.meezy.bc.collaboration.participation_metrics.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.participation_metrics.application.port.out.ParticipationCounterPort;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.exception.ParticipationSenderNotInMeetingException;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordParticipationService 테스트")
class RecordParticipationServiceTest {

    @Mock
    private ParticipationCounterPort participationCounterPort;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @Mock
    private MeetingRepository meetingRepository;

    @InjectMocks
    private RecordParticipationService recordParticipationService;

    private UUID meetingIdValue;
    private UUID userIdValue;
    private UserId userId;

    @BeforeEach
    void setUp() {
        meetingIdValue = UUID.randomUUID();
        userIdValue = UUID.randomUUID();
        userId = UserId.of(userIdValue);
    }

    private void mockCurrentUser() {
        given(currentUserQuery.currentUser())
                .willReturn(AuthenticatedUser.builder().userId(userId).build());
    }

    private Meeting createActiveMeetingWithParticipant(UUID participantId) {
        TeamId teamId = TeamId.of(UUID.randomUUID());
        Meeting meeting = Meeting.start(teamId, UserId.of(participantId));
        meeting.pullDomainEvents();
        return meeting;
    }

    @Nested
    @DisplayName("음성 참여 기록")
    class RecordVoiceTest {

        @Test
        @DisplayName("캐시 히트 - 활성 참가자는 음성 참여를 기록할 수 있다")
        void recordVoice_succeeds_for_active_participant_with_cache() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(userIdValue);
            given(meetingRepository.findByMeetingId_Value(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.of(Set.of(userIdValue)));

            recordParticipationService.recordVoice(meetingIdValue);

            verify(participationCounterPort).incrementVoiceCount(meetingIdValue, userIdValue);
        }

        @Test
        @DisplayName("캐시 히트 - 회의 참가자가 아니면 예외가 발생한다")
        void recordVoice_throws_when_not_participant_with_cache() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(userIdValue);
            given(meetingRepository.findByMeetingId_Value(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            UUID otherUserId = UUID.randomUUID();
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.of(Set.of(otherUserId)));

            assertThatThrownBy(() -> recordParticipationService.recordVoice(meetingIdValue))
                    .isInstanceOf(ParticipationSenderNotInMeetingException.class);

            verify(participationCounterPort, never()).incrementVoiceCount(any(), any());
        }

        @Test
        @DisplayName("비활성 회의에는 무시되고 캐시가 제거된다")
        void recordVoice_ignores_inactive_meeting_and_evicts_cache() {
            mockCurrentUser();
            given(meetingRepository.findByMeetingId_Value(meetingIdValue))
                    .willReturn(Optional.empty());

            recordParticipationService.recordVoice(meetingIdValue);

            verify(participationCounterPort).evictCachedParticipantIds(meetingIdValue);
            verify(participationCounterPort, never()).incrementVoiceCount(any(), any());
        }

        @Test
        @DisplayName("캐시 미스 시 DB에서 조회하여 참가자를 검증한다")
        void recordVoice_loads_from_db_on_cache_miss() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(userIdValue);
            given(meetingRepository.findByMeetingId_Value(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.empty());

            recordParticipationService.recordVoice(meetingIdValue);

            verify(participationCounterPort).cacheParticipantIds(eq(meetingIdValue), any());
            verify(participationCounterPort).incrementVoiceCount(meetingIdValue, userIdValue);
        }

        @Test
        @DisplayName("캐시 미스 시 비참가자는 예외가 발생한다")
        void recordVoice_throws_on_cache_miss_when_not_participant() {
            mockCurrentUser();
            UUID otherUserId = UUID.randomUUID();
            Meeting meeting = createActiveMeetingWithParticipant(otherUserId);
            given(meetingRepository.findByMeetingId_Value(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> recordParticipationService.recordVoice(meetingIdValue))
                    .isInstanceOf(ParticipationSenderNotInMeetingException.class);
        }
    }

    @Nested
    @DisplayName("채팅 참여 기록")
    class RecordChatTest {

        @Test
        @DisplayName("활성 참가자는 채팅 참여를 기록할 수 있다")
        void recordChat_succeeds_for_active_participant() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(userIdValue);
            given(meetingRepository.findByMeetingId_Value(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.of(Set.of(userIdValue)));

            recordParticipationService.recordChat(meetingIdValue);

            verify(participationCounterPort).incrementChatCount(meetingIdValue, userIdValue);
        }

        @Test
        @DisplayName("회의 참가자가 아니면 예외가 발생한다")
        void recordChat_throws_when_not_participant() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(userIdValue);
            given(meetingRepository.findByMeetingId_Value(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            UUID otherUserId = UUID.randomUUID();
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.of(Set.of(otherUserId)));

            assertThatThrownBy(() -> recordParticipationService.recordChat(meetingIdValue))
                    .isInstanceOf(ParticipationSenderNotInMeetingException.class);

            verify(participationCounterPort, never()).incrementChatCount(any(), any());
        }
    }
}
