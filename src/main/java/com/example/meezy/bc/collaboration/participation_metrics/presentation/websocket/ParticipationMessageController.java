package com.example.meezy.bc.collaboration.participation_metrics.presentation.websocket;

import com.example.meezy.bc.collaboration.participation_metrics.application.service.RecordParticipationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ParticipationMessageController {

    private final RecordParticipationService recordParticipationService;

    @MessageMapping("/meetings/{meetingId}/participation/voice")
    public void recordVoice(@DestinationVariable UUID meetingId) {
        log.debug("Received STOMP participation voice event: meetingId={}", meetingId);
        recordParticipationService.recordVoice(meetingId);
    }

    @MessageMapping("/meetings/{meetingId}/participation/chat")
    public void recordChat(@DestinationVariable UUID meetingId) {
        log.debug("Received STOMP participation chat event: meetingId={}", meetingId);
        recordParticipationService.recordChat(meetingId);
    }
}
