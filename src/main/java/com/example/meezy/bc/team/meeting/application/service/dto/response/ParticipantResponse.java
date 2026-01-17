package com.example.meezy.bc.team.meeting.application.service.dto.response;

import com.example.meezy.bc.team.meeting.domain.MeetingParticipant;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ParticipantResponse(
        UUID participantId,
        UUID userId,
        LocalDateTime joinedAt
) {

    public static ParticipantResponse from(MeetingParticipant participant) {
        return ParticipantResponse.builder()
                .participantId(participant.getMeetingParticipantId().value())
                .userId(participant.getUserId().value())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}
