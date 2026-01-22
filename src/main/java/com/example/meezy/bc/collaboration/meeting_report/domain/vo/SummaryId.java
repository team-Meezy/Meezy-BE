package com.example.meezy.bc.collaboration.meeting_report.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record SummaryId(
        UUID value
) {

    public static SummaryId newId() {
        return new SummaryId(UUID.randomUUID());
    }

    public static SummaryId of(UUID id) {
        return new SummaryId(id);
    }
}
