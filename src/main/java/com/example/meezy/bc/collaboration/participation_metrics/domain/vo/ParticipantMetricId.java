package com.example.meezy.bc.collaboration.participation_metrics.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ParticipantMetricId(
        UUID value
) {

    public static ParticipantMetricId newId() {
        return new ParticipantMetricId(UUID.randomUUID());
    }

    public static ParticipantMetricId of(UUID id) {
        return new ParticipantMetricId(id);
    }
}
