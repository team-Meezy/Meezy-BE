package com.example.meezy.bc.team.meeting.application.service;

import com.example.meezy.bc.team.meeting.application.port.out.SignalMessagePublisher;
import com.example.meezy.bc.team.meeting.application.service.dto.request.SignalMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelaySignalService {

    private final SignalMessagePublisher signalMessagePublisher;

    public void relay(UUID teamId, SignalMessage message) {
        signalMessagePublisher.publish(teamId, message);
    }
}
