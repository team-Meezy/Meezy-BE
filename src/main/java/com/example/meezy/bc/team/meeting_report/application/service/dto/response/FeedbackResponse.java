package com.example.meezy.bc.team.meeting_report.application.service.dto.response;

import com.example.meezy.bc.team.meeting_report.domain.Feedback;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FeedbackResponse(
        UUID feedbackId,
        UUID meetingId,
        UUID teamId,
        String content,
        LocalDateTime createdAt
) {

    public static FeedbackResponse from(Feedback feedback, UUID teamId) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId().value())
                .meetingId(feedback.getMeetingId().value())
                .teamId(teamId)
                .content(feedback.getContent())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
