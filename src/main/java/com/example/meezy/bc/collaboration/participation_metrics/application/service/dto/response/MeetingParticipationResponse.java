package com.example.meezy.bc.collaboration.participation_metrics.application.service.dto.response;

import com.example.meezy.bc.collaboration.participation_metrics.domain.ParticipationMetrics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MeetingParticipationResponse(
        UUID meetingId,
        UUID teamId,
        long meetingDurationSeconds,
        LocalDateTime createdAt,
        List<ParticipantMetricResponse> participants
) {
    public static MeetingParticipationResponse from(ParticipationMetrics metrics) {
        List<ParticipantMetricResponse> participants = metrics.getParticipantMetrics().stream()
                .map(ParticipantMetricResponse::from)
                .toList();

        return new MeetingParticipationResponse(
                metrics.getMeetingId().value(),
                metrics.getTeamId().value(),
                metrics.getMeetingDurationSeconds(),
                metrics.getCreatedAt(),
                participants
        );
    }
}
