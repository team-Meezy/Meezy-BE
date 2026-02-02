package com.example.meezy.bc.collaboration.meeting_report.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record MeetingReportId(
        UUID value
) {

    public static MeetingReportId newId() {
        return new MeetingReportId(UUID.randomUUID());
    }

    public static MeetingReportId of(UUID id) {
        return new MeetingReportId(id);
    }
}
