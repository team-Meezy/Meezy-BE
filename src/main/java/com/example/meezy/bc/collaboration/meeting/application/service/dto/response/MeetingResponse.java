package com.example.meezy.bc.collaboration.meeting.application.service.dto.response;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.MeetingParticipant;
import com.example.meezy.bc.user.user.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Builder
public record MeetingResponse(
        UUID meetingId,
        UUID teamId,
        UUID hostUserId,
        String status,
        LocalDateTime startedAt,
        List<ParticipantResponse> participants,
        List<IceServerResponse> iceServers
) {

    public static MeetingResponse from(Meeting meeting, Map<UUID, User> users, List<IceServerResponse> iceServers) {
        return MeetingResponse.builder()
                .meetingId(meeting.getMeetingId().value())
                .teamId(meeting.getTeamId().value())
                .hostUserId(meeting.getHostUserId().value())
                .status(meeting.getStatus().name())
                .startedAt(meeting.getStartedAt())
                .participants(meeting.getParticipants().stream()
                        .filter(MeetingParticipant::isActive)
                        .map(p -> ParticipantResponse.from(p, users.get(p.getUserId().value())))
                        .toList())
                .iceServers(iceServers)
                .build();
    }
}
