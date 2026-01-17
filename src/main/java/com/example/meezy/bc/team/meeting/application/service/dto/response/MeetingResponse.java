package com.example.meezy.bc.team.meeting.application.service.dto.response;

import com.example.meezy.bc.team.meeting.domain.Meeting;
import com.example.meezy.bc.team.meeting.domain.MeetingParticipant;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record MeetingResponse(
        UUID meetingId,
        UUID teamId,
        UUID hostUserId,
        String status,
        LocalDateTime startedAt,
        List<ParticipantResponse> participants
) {

    public static MeetingResponse from(Meeting meeting) {
        return MeetingResponse.builder()
                .meetingId(meeting.getMeetingId().value())
                .teamId(meeting.getTeamId().value())
                .hostUserId(meeting.getHostUserId().value())
                .status(meeting.getStatus().name())
                .startedAt(meeting.getStartedAt())
                .participants(meeting.getParticipants().stream()
                        .filter(MeetingParticipant::isActive)
                        .map(ParticipantResponse::from)
                        .toList())
                .build();
    }
}
