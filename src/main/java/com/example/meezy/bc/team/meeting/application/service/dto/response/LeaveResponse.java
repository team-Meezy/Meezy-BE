package com.example.meezy.bc.team.meeting.application.service.dto.response;

import lombok.Builder;

@Builder
public record LeaveResponse(
        boolean isMeetingActive
) {
}
