package com.example.meezy.bc.team.team.application.service.dto.response;

import com.example.meezy.bc.team.team.domain.Team;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TeamResponse(
        UUID teamId,
        String teamName,
        String serverImageUrl
) {

    public static TeamResponse from(Team team){
        return TeamResponse.builder()
                .teamId(team.getTeamId().value())
                .teamName(team.getName())
                .serverImageUrl(team.getServerImageUrl())
                .build();
    }
}
