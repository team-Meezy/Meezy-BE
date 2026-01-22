package com.example.meezy.bc.collaboration.team.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record TeamMemberId(
        UUID value
) {

    public static TeamMemberId newId(){
        return new TeamMemberId(UUID.randomUUID());
    }

    public static TeamMemberId of(UUID memberId){
        return new TeamMemberId(memberId);
    }
}
