package com.example.meezy.bc.collaboration.participation_metrics.presentation.websocket;

import com.example.meezy.bc.collaboration.participation_metrics.application.service.RecordParticipationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ParticipationMessageController {

    private final RecordParticipationService recordParticipationService;

    @MessageMapping("/meetings/{meetingId}/participation/voice")
    public void recordVoice(@DestinationVariable UUID meetingId) {
        recordParticipationService.recordVoice(meetingId);
    }

    @MessageMapping("/meetings/{meetingId}/participation/chat")
    public void recordChat(@DestinationVariable UUID meetingId) {
        recordParticipationService.recordChat(meetingId);
    }
}
