package com.example.meezy.bc.collaboration.participation_metrics.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.dto.response.MeetingParticipationResponse;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.dto.response.ParticipationResponse;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.exception.ParticipationMetricsNotFoundException;
import com.example.meezy.bc.collaboration.participation_metrics.domain.ParticipationMetrics;
import com.example.meezy.bc.collaboration.meeting.domain.exception.NotTeamMemberException;
import com.example.meezy.bc.collaboration.participation_metrics.domain.repository.ParticipationMetricsRepository;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryParticipationService 테스트")
class QueryParticipationServiceTest {

    @Mock
    private ParticipationMetricsRepository participationMetricsRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @InjectMocks
    private QueryParticipationService queryParticipationService;

    private UUID teamIdValue;
    private UUID meetingIdValue;
    private UUID userIdValue;
    private MeetingId meetingId;
    private TeamId teamId;
    private UserId userId;
    private ParticipationMetrics participationMetrics;

    @BeforeEach
    void setUp() {
        teamIdValue = UUID.randomUUID();
        meetingIdValue = UUID.randomUUID();
        userIdValue = UUID.randomUUID();
        meetingId = MeetingId.of(meetingIdValue);
        teamId = TeamId.of(teamIdValue);
        userId = UserId.of(userIdValue);

        Map<UUID, Long> connectionDurations = new HashMap<>();
        connectionDurations.put(userIdValue, 3600L);

        Map<UUID, Integer> voiceCounts = new HashMap<>();
        voiceCounts.put(userIdValue, 10);

        Map<UUID, Integer> chatCounts = new HashMap<>();
        chatCounts.put(userIdValue, 5);

        participationMetrics = ParticipationMetrics.create(
                meetingId,
                teamId,
                3600L,
                List.of(userId),
                connectionDurations,
                voiceCounts,
                chatCounts
        );
    }

    @Nested
    @DisplayName("개인 참여 지표 조회")
    class GetParticipationTest {

        @Test
        @DisplayName("현재 참여율과 평균 참여율을 조회할 수 있다")
        void getParticipation_returns_current_and_average_rate() {
            given(currentUserQuery.currentUser())
                    .willReturn(AuthenticatedUser.builder().userId(userId).build());
            given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                    .willReturn(true);
            given(participationMetricsRepository.findByMeetingIdWithParticipants(any(MeetingId.class)))
                    .willReturn(Optional.of(participationMetrics));
            given(participationMetricsRepository.findRecentRatesByTeamIdAndUserId(any(TeamId.class), any(UserId.class)))
                    .willReturn(List.of(0.8, 0.7, 0.9));

            ParticipationResponse response = queryParticipationService.getParticipation(
                    teamIdValue, meetingIdValue);

            assertThat(response.meetingId()).isEqualTo(meetingIdValue);
            assertThat(response.userId()).isEqualTo(userIdValue);
            assertThat(response.currentRate()).isGreaterThan(0);
            assertThat(response.averageRate()).isCloseTo(0.8, org.assertj.core.api.Assertions.within(0.001));
            assertThat(response.meetingCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("팀 멤버가 아니면 예외가 발생한다")
        void getParticipation_throws_when_not_team_member() {
            given(currentUserQuery.currentUser())
                    .willReturn(AuthenticatedUser.builder().userId(userId).build());
            given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                    .willReturn(false);

            assertThatThrownBy(() -> queryParticipationService.getParticipation(
                    teamIdValue, meetingIdValue))
                    .isInstanceOf(NotTeamMemberException.class);
        }

        @Test
        @DisplayName("참여 지표가 없으면 예외가 발생한다")
        void getParticipation_throws_when_not_found() {
            given(currentUserQuery.currentUser())
                    .willReturn(AuthenticatedUser.builder().userId(userId).build());
            given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                    .willReturn(true);
            given(participationMetricsRepository.findByMeetingIdWithParticipants(any(MeetingId.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> queryParticipationService.getParticipation(
                    teamIdValue, meetingIdValue))
                    .isInstanceOf(ParticipationMetricsNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("회의 참여 지표 조회")
    class GetMeetingParticipationTest {

        @Test
        @DisplayName("회의의 모든 참가자 지표를 조회할 수 있다")
        void getMeetingParticipation_returns_all_participant_metrics() {
            given(currentUserQuery.currentUser())
                    .willReturn(AuthenticatedUser.builder().userId(userId).build());
            given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                    .willReturn(true);
            given(participationMetricsRepository.findByMeetingIdWithParticipants(any(MeetingId.class)))
                    .willReturn(Optional.of(participationMetrics));

            MeetingParticipationResponse response = queryParticipationService.getMeetingParticipation(teamIdValue, meetingIdValue);

            assertThat(response.meetingId()).isEqualTo(meetingIdValue);
            assertThat(response.teamId()).isEqualTo(teamIdValue);
            assertThat(response.participants()).hasSize(1);
        }

        @Test
        @DisplayName("참여 지표가 없으면 예외가 발생한다")
        void getMeetingParticipation_throws_when_not_found() {
            given(currentUserQuery.currentUser())
                    .willReturn(AuthenticatedUser.builder().userId(userId).build());
            given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class)))
                    .willReturn(true);
            given(participationMetricsRepository.findByMeetingIdWithParticipants(any(MeetingId.class)))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> queryParticipationService.getMeetingParticipation(teamIdValue, meetingIdValue))
                    .isInstanceOf(ParticipationMetricsNotFoundException.class);
        }

        @Test
        @DisplayName("회의가 해당 팀 소속이 아니면 예외가 발생한다")
        void getMeetingParticipation_throws_when_team_mismatch() {
            UUID otherTeamIdValue = UUID.randomUUID();
            given(currentUserQuery.currentUser())
                    .willReturn(AuthenticatedUser.builder().userId(userId).build());
            given(teamRepository.existsMemberByTeamIdAndUserId(eq(otherTeamIdValue), any(UserId.class)))
                    .willReturn(true);
            given(participationMetricsRepository.findByMeetingIdWithParticipants(any(MeetingId.class)))
                    .willReturn(Optional.of(participationMetrics));

            assertThatThrownBy(() -> queryParticipationService.getMeetingParticipation(otherTeamIdValue, meetingIdValue))
                    .isInstanceOf(ParticipationMetricsNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("평균 참여율 조회")
    class GetAverageRateTest {

        @Test
        @DisplayName("최근 30개 회의의 평균 참여율을 계산한다")
        void getAverageRate_calculates_average_of_recent_meetings() {
            given(participationMetricsRepository.findRecentRatesByTeamIdAndUserId(any(TeamId.class), any(UserId.class)))
                    .willReturn(List.of(0.8, 0.6, 0.7));

            double averageRate = queryParticipationService.getAverageRate(teamIdValue, userIdValue);

            assertThat(averageRate).isCloseTo(0.7, org.assertj.core.api.Assertions.within(0.001));
        }

        @Test
        @DisplayName("참여 기록이 없으면 0을 반환한다")
        void getAverageRate_returns_zero_when_no_records() {
            given(participationMetricsRepository.findRecentRatesByTeamIdAndUserId(any(TeamId.class), any(UserId.class)))
                    .willReturn(List.of());

            double averageRate = queryParticipationService.getAverageRate(teamIdValue, userIdValue);

            assertThat(averageRate).isZero();
        }
    }
}
