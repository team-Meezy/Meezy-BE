package com.example.meezy.bc.collaboration.team.domain.vo;

import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
public record InviteCode(
        String code,
        LocalDateTime expiresAt
) {

    public static InviteCode generate(){
        String code = UUID.randomUUID().toString().substring(0, 8);
        return new InviteCode(code, LocalDateTime.now().plusMinutes(5));
    }

    public static InviteCode of(String code, LocalDateTime expiresAt){
        return new InviteCode(code, expiresAt);
    }

    public boolean isExpired(){
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
