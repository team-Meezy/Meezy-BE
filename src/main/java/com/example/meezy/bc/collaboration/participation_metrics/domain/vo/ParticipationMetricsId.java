package com.example.meezy.bc.collaboration.participation_metrics.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ParticipationMetricsId(
        UUID value
) {

    public static ParticipationMetricsId newId(){
        return new ParticipationMetricsId(UUID.randomUUID());
    }

    public static ParticipationMetricsId of(UUID participationMetricsId){
        return new ParticipationMetricsId(participationMetricsId);
    }
}
