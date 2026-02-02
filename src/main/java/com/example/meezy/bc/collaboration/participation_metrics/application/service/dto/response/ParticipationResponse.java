package com.example.meezy.bc.collaboration.participation_metrics.application.service.dto.response;

import java.util.UUID;

public record ParticipationResponse(
        UUID meetingId,
        UUID userId,
        double currentRate,
        double averageRate,
        int meetingCount
) {
    public static ParticipationResponse of(
            UUID meetingId,
            UUID userId,
            double currentRate,
            double averageRate,
            int meetingCount
    ) {
        return new ParticipationResponse(
                meetingId,
                userId,
                currentRate,
                averageRate,
                meetingCount
        );
    }
}
