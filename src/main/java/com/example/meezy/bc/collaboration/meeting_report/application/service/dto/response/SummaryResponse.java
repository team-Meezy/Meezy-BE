package com.example.meezy.bc.collaboration.meeting_report.application.service.dto.response;

import com.example.meezy.bc.collaboration.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.collaboration.meeting_report.domain.Summary;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SummaryResponse(
        UUID summaryId,
        UUID meetingId,
        UUID teamId,
        String title,
        String content,
        LocalDateTime createdAt
) {

    public static SummaryResponse from(MeetingReport report, UUID teamId) {
        Summary summary = report.getSummary();
        return SummaryResponse.builder()
                .summaryId(summary.getSummaryId().value())
                .meetingId(summary.getMeetingId().value())
                .teamId(teamId)
                .title(report.getTitle())
                .content(summary.getContent())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}
