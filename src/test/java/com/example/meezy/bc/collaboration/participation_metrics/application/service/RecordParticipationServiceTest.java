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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecordParticipationService tests")
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
    @DisplayName("recordVoice")
    class RecordVoiceTest {

        @Test
        @DisplayName("increments voice count when cached participant matches current user")
        void recordVoice_succeeds_for_active_participant_with_cache() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(userIdValue);
            given(meetingRepository.findByMeetingIdWithParticipants(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.of(Set.of(userIdValue)));

            recordParticipationService.recordVoice(meetingIdValue);

            verify(participationCounterPort).incrementVoiceCount(meetingIdValue, userIdValue);
        }

        @Test
        @DisplayName("refreshes stale cache and still increments voice count for an active participant")
        void recordVoice_recovers_from_stale_cache_for_active_participant() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(userIdValue);
            given(meetingRepository.findByMeetingIdWithParticipants(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.of(Set.of(UUID.randomUUID())));

            recordParticipationService.recordVoice(meetingIdValue);

            verify(participationCounterPort).cacheParticipantIds(eq(meetingIdValue), any());
            verify(participationCounterPort).incrementVoiceCount(meetingIdValue, userIdValue);
        }

        @Test
        @DisplayName("drops voice events for inactive meetings and evicts the participant cache")
        void recordVoice_ignores_inactive_meeting_and_evicts_cache() {
            mockCurrentUser();
            given(meetingRepository.findByMeetingIdWithParticipants(meetingIdValue))
                    .willReturn(Optional.empty());

            recordParticipationService.recordVoice(meetingIdValue);

            verify(participationCounterPort).evictCachedParticipantIds(meetingIdValue);
            verify(participationCounterPort, never()).incrementVoiceCount(any(), any());
        }

        @Test
        @DisplayName("loads active participants from the meeting when the cache is empty")
        void recordVoice_loads_from_db_on_cache_miss() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(userIdValue);
            given(meetingRepository.findByMeetingIdWithParticipants(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.empty());

            recordParticipationService.recordVoice(meetingIdValue);

            verify(participationCounterPort).cacheParticipantIds(eq(meetingIdValue), any());
            verify(participationCounterPort).incrementVoiceCount(meetingIdValue, userIdValue);
        }

        @Test
        @DisplayName("throws when the current user is not in the refreshed active participant set")
        void recordVoice_throws_on_cache_miss_when_not_participant() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(UUID.randomUUID());
            given(meetingRepository.findByMeetingIdWithParticipants(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> recordParticipationService.recordVoice(meetingIdValue))
                    .isInstanceOf(ParticipationSenderNotInMeetingException.class);
        }
    }

    @Nested
    @DisplayName("recordChat")
    class RecordChatTest {

        @Test
        @DisplayName("increments chat count when cached participant matches current user")
        void recordChat_succeeds_for_active_participant() {
            mockCurrentUser();
            Meeting meeting = createActiveMeetingWithParticipant(userIdValue);
            given(meetingRepository.findByMeetingIdWithParticipants(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.of(Set.of(userIdValue)));

            recordParticipationService.recordChat(meetingIdValue);

            verify(participationCounterPort).incrementChatCount(meetingIdValue, userIdValue);
        }

        @Test
        @DisplayName("throws when the current user is still not an active participant after refresh")
        void recordChat_throws_when_not_participant() {
            mockCurrentUser();
            UUID otherUserId = UUID.randomUUID();
            Meeting meeting = createActiveMeetingWithParticipant(otherUserId);
            given(meetingRepository.findByMeetingIdWithParticipants(meetingIdValue))
                    .willReturn(Optional.of(meeting));
            given(participationCounterPort.getCachedParticipantIds(meetingIdValue))
                    .willReturn(Optional.of(Set.of(otherUserId)));

            assertThatThrownBy(() -> recordParticipationService.recordChat(meetingIdValue))
                    .isInstanceOf(ParticipationSenderNotInMeetingException.class);

            verify(participationCounterPort, never()).incrementChatCount(any(), any());
        }
    }
}
