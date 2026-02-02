package com.example.meezy.bc.collaboration.participation_metrics.domain;

import com.example.meezy.bc.collaboration.participation_metrics.domain.vo.ParticipantMetricId;
import com.example.meezy.bc.collaboration.participation_metrics.domain.vo.ParticipationRate;
import com.example.meezy.bc.user.domain.vo.UserId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_participant_metric")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ParticipantMetric {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "participant_metric_id"))
    private ParticipantMetricId participantMetricId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_metrics_id", nullable = false)
    private ParticipationMetrics participationMetrics;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(nullable = false)
    private int voiceCount;

    @Column(nullable = false)
    private int chatCount;

    @Column(nullable = false)
    private long connectionSeconds;

    @Embedded
    private ParticipationRate participationRate;

    static ParticipantMetric create(
            UserId userId,
            int voiceCount,
            int chatCount,
            long connectionSeconds,
            ParticipationMetrics participationMetrics
    ) {
        return ParticipantMetric.builder()
                .participantMetricId(ParticipantMetricId.newId())
                .participationMetrics(participationMetrics)
                .userId(userId)
                .voiceCount(voiceCount)
                .chatCount(chatCount)
                .connectionSeconds(connectionSeconds)
                .participationRate(ParticipationRate.zero())
                .build();
    }

    void calculateParticipationRate(int maxVoiceCount, int maxChatCount, long meetingSeconds) {
        this.participationRate = ParticipationRate.calculate(
                voiceCount, maxVoiceCount,
                connectionSeconds, meetingSeconds,
                chatCount, maxChatCount
        );
    }

    public boolean hasParticipated() {
        return connectionSeconds > 0;
    }

    boolean hasUserId(UserId userId) {
        return this.userId.equals(userId);
    }
}
