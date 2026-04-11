package com.example.meezy.bc.collaboration.meeting.presentation.controller;

import com.example.meezy.bc.collaboration.meeting.application.service.JoinMeetingService;
import com.example.meezy.bc.collaboration.meeting.application.service.LeaveMeetingService;
import com.example.meezy.bc.collaboration.meeting.application.service.QueryMeetingService;
import com.example.meezy.bc.collaboration.meeting.application.service.ReceiveRecordingService;
import com.example.meezy.bc.collaboration.meeting.application.service.StartMeetingService;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.request.ReceiveRecordingRequest;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.LeaveResponse;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.MeetingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    //李몄뿬 媛?ν븳 ?뚯쓽媛 議댁옱???섎룄 ?덉쑝硫??놁쓣 ?섎룄 ?덉쑝誘濡? ResponseEntity瑜??쒖슜?섏뿬 ?덉쟾?섍쾶 諛섑솚
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
    public ResponseEntity<Void> receiveRecording(
            @PathVariable UUID teamId,
            @PathVariable UUID meetingId,
            @Valid @ModelAttribute ReceiveRecordingRequest request
    ) {
        receiveRecordingService.receive(teamId, meetingId, request.file(), request.title());
        return ResponseEntity.accepted().build();
    }
}
