package com.example.meezy.bc.collaboration.participation_metrics.application.service.dto.response;

import com.example.meezy.bc.collaboration.participation_metrics.domain.ParticipantMetric;

import java.util.UUID;

public record ParticipantMetricResponse(
        UUID userId,
        int voiceCount,
        int chatCount,
        long connectionSeconds,
        double participationRate,
        boolean participated
) {
    public static ParticipantMetricResponse from(ParticipantMetric metric) {
        return new ParticipantMetricResponse(
                metric.getUserId().value(),
                metric.getVoiceCount(),
                metric.getChatCount(),
                metric.getConnectionSeconds(),
                metric.getParticipationRate().getValue(),
                metric.hasParticipated()
        );
    }
}
