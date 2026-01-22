package com.example.meezy.bc.collaboration.meeting_report.presentation.controller;

import com.example.meezy.bc.collaboration.meeting_report.application.service.QuerySummaryService;
import com.example.meezy.bc.collaboration.meeting_report.application.service.dto.response.SummaryResponse;
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
public class SummaryController {

    private final QuerySummaryService querySummaryService;

    @GetMapping("/meetings/{meetingId}/summary")
    public SummaryResponse findByMeetingId(
            @PathVariable UUID teamId,
            @PathVariable UUID meetingId
    ) {
        return querySummaryService.findByMeetingId(teamId, meetingId);
    }

    @GetMapping("/summaries")
    public List<SummaryResponse> findAll(@PathVariable UUID teamId) {
        return querySummaryService.findAllByTeamId(teamId);
    }
}
