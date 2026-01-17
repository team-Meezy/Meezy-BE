package com.example.meezy.bc.team.meeting.presentation.controller;

import com.example.meezy.bc.team.meeting.application.service.RelaySignalService;
import com.example.meezy.bc.team.meeting.application.service.dto.request.SignalMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MeetingSignalingController {

    private final RelaySignalService relaySignalService;

    @MessageMapping("/teams/{teamId}/meeting/signal")
    public void handleSignal(
            @DestinationVariable UUID teamId,
            @Payload SignalMessage message
    ) {

        relaySignalService.relay(teamId, message);
    }
}
