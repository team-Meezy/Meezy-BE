package com.example.meezy.bc.team.team.application.service.dto.response;

import com.example.meezy.bc.team.team.domain.vo.InviteCode;

import java.time.LocalDateTime;

public record InviteCodeResponse(
        String inviteCode,
        LocalDateTime expiresAt
) {

    public static InviteCodeResponse from(InviteCode inviteCode){
        return new InviteCodeResponse(inviteCode.code(), inviteCode.expiresAt());
    }
}
