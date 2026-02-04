package com.example.meezy.bc.collaboration.meeting.application.service.dto.response;

import com.example.meezy.bc.collaboration.meeting.domain.MeetingParticipant;
import com.example.meezy.bc.user.user.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ParticipantResponse(
        UUID participantId,
        UUID userId,
        String name,
        String profileImageUrl,
        LocalDateTime joinedAt
) {

    public static ParticipantResponse from(MeetingParticipant participant, User user) {
        return ParticipantResponse.builder()
                .participantId(participant.getMeetingParticipantId().value())
                .userId(participant.getUserId().value())
                .name(user != null ? user.getName() : null)
                .profileImageUrl(user != null ? user.getProfileImageUrl() : null)
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}
