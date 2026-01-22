package com.example.meezy.bc.collaboration.team.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamId(
        UUID value
) {

    public static TeamId newId(){
        return new TeamId(UUID.randomUUID());
    }

    public static TeamId of(UUID teamId){
        return new TeamId(teamId);
    }
}
