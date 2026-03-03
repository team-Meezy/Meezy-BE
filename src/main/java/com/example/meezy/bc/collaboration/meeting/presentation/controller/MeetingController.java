package com.example.meezy.bc.collaboration.meeting.presentation.controller;

import com.example.meezy.bc.collaboration.meeting.application.service.*;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.request.ReceiveRecordingRequest;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.LeaveResponse;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.MeetingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    //참여 가능한 회의가 존재할 수도 있으며 없을 수도 있으므로, ResponseEntity를 활용하여 안전하게 반환
    @GetMapping("/active")
    public ResponseEntity<MeetingResponse> getActiveMeeting(@PathVariable UUID teamId) {
        return queryMeetingService.findActiveMeeting(teamId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/join")
    public MeetingResponse join(@PathVariable UUID teamId) {
        return joinMeetingService.join(teamId);
    }

    @PostMapping("/leave")
    public LeaveResponse leave(@PathVariable UUID teamId) {
        return leaveMeetingService.leave(teamId);
    }

    @PostMapping("/{meetingId}/recording")
    public void receiveRecording(
            @PathVariable UUID teamId,
            @PathVariable UUID meetingId,
            @Valid @ModelAttribute ReceiveRecordingRequest request
    ) {
        System.out.println("123232");
        receiveRecordingService.receive(teamId, meetingId, request.file());
    }

}
