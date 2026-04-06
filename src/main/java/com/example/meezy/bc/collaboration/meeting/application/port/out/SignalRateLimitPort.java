package com.example.meezy.bc.collaboration.meeting.application.port.out;

import java.util.UUID;

public interface SignalRateLimitPort {

    void validate(UUID userId, UUID teamId);
}
