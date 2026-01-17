package com.example.meezy.bc.team.meeting.infrastructure.adapter.out.messaging.dto;

import java.util.UUID;

public record SignalPayload(
        String type,
        UUID fromUserId,
        UUID toUserId,
        String sdp,
        String candidate,
        String sdpMid,
        Integer sdpMLineIndex
) {}
