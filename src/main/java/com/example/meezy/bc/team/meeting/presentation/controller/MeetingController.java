package com.example.meezy.bc.team.meeting.presentation.controller;

import com.example.meezy.bc.team.meeting.application.service.*;
import com.example.meezy.bc.team.meeting.application.service.dto.response.MeetingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/teams/{teamId}/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final StartMeetingService startMeetingService;
    private final JoinMeetingService joinMeetingService;
    private final LeaveMeetingService leaveMeetingService;
    private final QueryMeetingService queryMeetingService;
    private final ReceiveRecordingService receiveRecordingService;

    @PostMapping
    public MeetingResponse start(@PathVariable UUID teamId) {
        return startMeetingService.start(teamId);
    }

    @GetMapping("/active")
    public ResponseEntity<MeetingResponse> getActiveMeeting(@PathVariable UUID teamId) {
        return queryMeetingService.findActiveMeeting(teamId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/join")
    public MeetingResponse join(@PathVariable UUID teamId) {
        return joinMeetingService.join(teamId);
    }

    @PostMapping("/leave")
    public LeaveResponse leave(@PathVariable UUID teamId) {
        boolean isMeetingActive = leaveMeetingService.leave(teamId);
        return new LeaveResponse(isMeetingActive);
    }

    @PostMapping("/{meetingId}/recording")
    public void receiveRecording(
            @PathVariable UUID teamId,
            @PathVariable UUID meetingId,
            @RequestParam("file") MultipartFile recording
    ) {
        receiveRecordingService.receive(teamId, meetingId, recording);
    }

    public record LeaveResponse(boolean isMeetingActive) {}
}
