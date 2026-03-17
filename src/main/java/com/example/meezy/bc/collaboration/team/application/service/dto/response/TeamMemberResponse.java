package com.example.meezy.bc.collaboration.team.application.service.dto.response;

import com.example.meezy.bc.collaboration.team.domain.TeamMember;

import java.util.UUID;

public record TeamMemberResponse(
        UUID teamMemberId,
        String name,
        String profileImageUrl,
        String role
) {

    public static TeamMemberResponse from(TeamMember teamMember, String name, String profileImageUrl) {
        return new TeamMemberResponse(
                teamMember.getTeamMemberId().value(),
                name,
                profileImageUrl,
                teamMember.getRole().name()
        );
    }
}
