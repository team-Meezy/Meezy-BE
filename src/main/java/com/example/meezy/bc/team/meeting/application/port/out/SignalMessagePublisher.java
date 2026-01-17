package com.example.meezy.bc.team.meeting.application.port.out;

import com.example.meezy.bc.team.meeting.application.service.dto.request.SignalMessage;

import java.util.UUID;

public interface SignalMessagePublisher {

    void publish(UUID teamId, SignalMessage message);
}
