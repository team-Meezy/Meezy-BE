package com.example.meezy.bc.collaboration.participation_metrics.presentation.controller;

import com.example.meezy.bc.collaboration.participation_metrics.application.service.QueryParticipationService;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.dto.response.MeetingParticipationResponse;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.dto.response.ParticipationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/teams/{teamId}/meetings/{meetingId}/participation")
@RequiredArgsConstructor
public class ParticipationController {

    private final QueryParticipationService queryParticipationService;

    @GetMapping
    public MeetingParticipationResponse getMeetingParticipation(
            @PathVariable UUID teamId,
            @PathVariable UUID meetingId
    ) {
        return queryParticipationService.getMeetingParticipation(meetingId);
    }

    @GetMapping("/member")
    public ParticipationResponse getParticipation(
            @PathVariable UUID teamId,
            @PathVariable UUID meetingId
    ) {
        return queryParticipationService.getParticipation(teamId, meetingId);
    }
}
