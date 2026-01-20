package com.example.meezy.bc.team.meeting_report.application.service.dto.response;

import com.example.meezy.bc.team.meeting_report.domain.Summary;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record SummaryResponse(
        UUID summaryId,
        UUID meetingId,
        UUID teamId,
        String content,
        LocalDateTime createdAt
) {

    public static SummaryResponse from(Summary summary, UUID teamId){
        return SummaryResponse.builder()
                .summaryId(summary.getSummaryId().value())
                .meetingId(summary.getMeetingId().value())
                .teamId(teamId)
                .content(summary.getContent())
                .createdAt(summary.getCreatedAt())
                .build();
    }
}
