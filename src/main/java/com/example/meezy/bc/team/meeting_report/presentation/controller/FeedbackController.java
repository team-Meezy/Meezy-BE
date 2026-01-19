package com.example.meezy.bc.team.meeting_report.presentation.controller;

import com.example.meezy.bc.team.meeting_report.application.service.QueryFeedbackService;
import com.example.meezy.bc.team.meeting_report.application.service.dto.response.FeedbackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams/{teamId}")
@RequiredArgsConstructor
public class FeedbackController {

    private final QueryFeedbackService queryFeedbackService;

    @GetMapping("/meetings/{meetingId}/feedback")
    public FeedbackResponse findByMeetingId(
            @PathVariable UUID teamId,
            @PathVariable UUID meetingId
    ) {
        return queryFeedbackService.findByMeetingId(teamId, meetingId);
    }

    @GetMapping("/feedbacks")
    public List<FeedbackResponse> findAll(@PathVariable UUID teamId) {
        return queryFeedbackService.findAllByTeamId(teamId);
    }
}
