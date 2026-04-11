package com.example.meezy.bc.collaboration.meeting_report.application.service.dto.response;

import com.example.meezy.bc.collaboration.meeting_report.domain.Feedback;
import com.example.meezy.bc.collaboration.meeting_report.domain.MeetingReport;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FeedbackResponse(
        UUID feedbackId,
        UUID meetingId,
        UUID teamId,
        String title,
        String content,
        LocalDateTime createdAt
) {

    public static FeedbackResponse from(MeetingReport report, UUID teamId) {
        Feedback feedback = report.getFeedback();
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId().value())
                .meetingId(feedback.getMeetingId().value())
                .teamId(teamId)
                .title(report.getTitle())
                .content(feedback.getContent())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
