package com.example.meezy.bc.collaboration.participation_metrics.domain;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.participation_metrics.domain.vo.ParticipationMetricsId;
import com.example.meezy.bc.collaboration.participation_metrics.domain.vo.ParticipationRate;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
import com.example.meezy.bc.user.domain.vo.UserId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_participation_metrics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipationMetrics extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "participation_metrics_id"))
    private ParticipationMetricsId participationMetricsId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "meeting_id"))
    private MeetingId meetingId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "team_id"))
    private TeamId teamId;

    @Builder.Default
    @OneToMany(
            mappedBy = "participationMetrics",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ParticipantMetric> participantMetrics = new ArrayList<>();

    @Column(nullable = false)
    private long meetingDurationSeconds;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static ParticipationMetrics create(
            MeetingId meetingId,
            TeamId teamId,
            long meetingDurationSeconds,
            List<UserId> allTeamMemberIds,
            Map<UUID, Long> connectionDurations,
            Map<UUID, Integer> voiceCounts,
            Map<UUID, Integer> chatCounts
    ) {
        ParticipationMetrics metrics = ParticipationMetrics.builder()
                .participationMetricsId(ParticipationMetricsId.newId())
                .meetingId(meetingId)
                .teamId(teamId)
                .meetingDurationSeconds(meetingDurationSeconds)
                .createdAt(LocalDateTime.now())
                .build();

        allTeamMemberIds.forEach(userId -> {
            UUID id = userId.value();
            ParticipantMetric participantMetric = ParticipantMetric.create(
                    userId,
                    voiceCounts.getOrDefault(id, 0),
                    chatCounts.getOrDefault(id, 0),
                    connectionDurations.getOrDefault(id, 0L),
                    metrics
            );
            metrics.participantMetrics.add(participantMetric);
        });

        metrics.calculateAllParticipationRates();
        return metrics;
    }

    public ParticipationRate getParticipationRateByUserId(UserId userId) { //해당 유저의 참여율 계산 로직
        return participantMetrics.stream()
                .filter(metric -> metric.hasUserId(userId))
                .findFirst()
                .map(ParticipantMetric::getParticipationRate)
                .orElse(ParticipationRate.zero());
    }

    public List<ParticipantMetric> getParticipantMetrics() {
        return Collections.unmodifiableList(participantMetrics);
    }

    private void calculateAllParticipationRates() {
        int maxVoiceCount = findMaxVoiceCount();
        int maxChatCount = findMaxChatCount();

        participantMetrics.forEach(metric ->
                metric.calculateParticipationRate(maxVoiceCount, maxChatCount, meetingDurationSeconds)
        );
    }

    private int findMaxVoiceCount() {
        return participantMetrics.stream()
                .mapToInt(ParticipantMetric::getVoiceCount)
                .max()
                .orElse(0);
    }

    private int findMaxChatCount() {
        return participantMetrics.stream()
                .mapToInt(ParticipantMetric::getChatCount)
                .max()
                .orElse(0);
    }
}
