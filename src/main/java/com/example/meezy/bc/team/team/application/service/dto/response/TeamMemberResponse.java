package com.example.meezy.bc.team.team.application.service.dto.response;

import com.example.meezy.bc.team.team.domain.TeamMember;

import java.util.UUID;

public record TeamMemberResponse(
        UUID teamMemberId,
        String name,
        String role
) {

    public static TeamMemberResponse from(TeamMember teamMember, String name) {
        return new TeamMemberResponse(
                teamMember.getTeamMemberId().value(),
                name,
                teamMember.getRole().name()
        );
    }
}
