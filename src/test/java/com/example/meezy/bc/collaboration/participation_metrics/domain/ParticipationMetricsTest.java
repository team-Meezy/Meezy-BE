package com.example.meezy.bc.collaboration.participation_metrics.domain;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.participation_metrics.domain.vo.ParticipationRate;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ParticipationMetrics 도메인 테스트")
class ParticipationMetricsTest {

    private MeetingId meetingId;
    private TeamId teamId;
    private UserId user1Id;
    private UserId user2Id;
    private UserId user3Id;

    @BeforeEach
    void setUp() {
        meetingId = MeetingId.newId();
        teamId = TeamId.newId();
        user1Id = UserId.newId();
        user2Id = UserId.newId();
        user3Id = UserId.newId();
    }

    @Nested
    @DisplayName("참여 지표 생성")
    class CreateTest {

        @Test
        @DisplayName("팀 전체 멤버에 대해 참여 지표가 생성된다")
        void create_generates_metrics_for_all_team_members() {
            List<UserId> allMemberIds = List.of(user1Id, user2Id, user3Id);
            Map<UUID, Long> connectionDurations = new HashMap<>();
            connectionDurations.put(user1Id.value(), 3600L);
            connectionDurations.put(user2Id.value(), 1800L);

            Map<UUID, Integer> voiceCounts = new HashMap<>();
            voiceCounts.put(user1Id.value(), 10);
            voiceCounts.put(user2Id.value(), 5);

            Map<UUID, Integer> chatCounts = new HashMap<>();
            chatCounts.put(user1Id.value(), 3);

            ParticipationMetrics metrics = ParticipationMetrics.create(
                    meetingId,
                    teamId,
                    3600L,
                    allMemberIds,
                    connectionDurations,
                    voiceCounts,
                    chatCounts
            );

            assertThat(metrics.getParticipationMetricsId()).isNotNull();
            assertThat(metrics.getMeetingId()).isEqualTo(meetingId);
            assertThat(metrics.getTeamId()).isEqualTo(teamId);
            assertThat(metrics.getParticipantMetrics()).hasSize(3);
        }

        @Test
        @DisplayName("회의에 참여하지 않은 멤버는 0,0,0으로 생성된다")
        void create_sets_zero_for_non_participants() {
            List<UserId> allMemberIds = List.of(user1Id, user2Id, user3Id);
            Map<UUID, Long> connectionDurations = new HashMap<>();
            connectionDurations.put(user1Id.value(), 3600L);

            Map<UUID, Integer> voiceCounts = new HashMap<>();
            voiceCounts.put(user1Id.value(), 10);

            Map<UUID, Integer> chatCounts = new HashMap<>();

            ParticipationMetrics metrics = ParticipationMetrics.create(
                    meetingId,
                    teamId,
                    3600L,
                    allMemberIds,
                    connectionDurations,
                    voiceCounts,
                    chatCounts
            );

            ParticipantMetric nonParticipant = metrics.getParticipantMetrics().stream()
                    .filter(m -> m.getUserId().equals(user3Id))
                    .findFirst()
                    .orElseThrow();

            assertThat(nonParticipant.getVoiceCount()).isZero();
            assertThat(nonParticipant.getChatCount()).isZero();
            assertThat(nonParticipant.getConnectionSeconds()).isZero();
            assertThat(nonParticipant.hasParticipated()).isFalse();
        }
    }

    @Nested
    @DisplayName("참여율 계산")
    class ParticipationRateTest {

        @Test
        @DisplayName("가장 높은 발화자 기준으로 참여율이 계산된다")
        void calculates_rate_based_on_max_voice_count() {
            List<UserId> allMemberIds = List.of(user1Id, user2Id);

            Map<UUID, Long> connectionDurations = new HashMap<>();
            connectionDurations.put(user1Id.value(), 3600L);
            connectionDurations.put(user2Id.value(), 3600L);

            Map<UUID, Integer> voiceCounts = new HashMap<>();
            voiceCounts.put(user1Id.value(), 10);  // 최대
            voiceCounts.put(user2Id.value(), 5);   // 50%

            Map<UUID, Integer> chatCounts = new HashMap<>();

            ParticipationMetrics metrics = ParticipationMetrics.create(
                    meetingId,
                    teamId,
                    3600L,
                    allMemberIds,
                    connectionDurations,
                    voiceCounts,
                    chatCounts
            );

            ParticipationRate user1Rate = metrics.getParticipationRateByUserId(user1Id);
            ParticipationRate user2Rate = metrics.getParticipationRateByUserId(user2Id);

            // user1: voice(10/10)*0.5 + connection(3600/3600)*0.49 + chat(0/0)*0.01 = 0.99
            // user2: voice(5/10)*0.5 + connection(3600/3600)*0.49 + chat(0/0)*0.01 = 0.74
            assertThat(user1Rate.getValue()).isGreaterThan(user2Rate.getValue());
        }

        @Test
        @DisplayName("모든 지표가 0이면 참여율도 0이다")
        void calculates_zero_rate_when_all_metrics_are_zero() {
            List<UserId> allMemberIds = List.of(user1Id);

            ParticipationMetrics metrics = ParticipationMetrics.create(
                    meetingId,
                    teamId,
                    3600L,
                    allMemberIds,
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>()
            );

            ParticipationRate rate = metrics.getParticipationRateByUserId(user1Id);

            assertThat(rate.getValue()).isZero();
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 참여율은 0을 반환한다")
        void returns_zero_rate_for_unknown_user() {
            List<UserId> allMemberIds = List.of(user1Id);

            ParticipationMetrics metrics = ParticipationMetrics.create(
                    meetingId,
                    teamId,
                    3600L,
                    allMemberIds,
                    new HashMap<>(),
                    new HashMap<>(),
                    new HashMap<>()
            );

            ParticipationRate rate = metrics.getParticipationRateByUserId(UserId.newId());

            assertThat(rate.getValue()).isZero();
        }
    }
}
