package com.example.meezy.bc.collaboration.team.presentation.controller;

import com.example.meezy.bc.collaboration.team.application.service.dto.request.TeamJoinRequest;
import com.example.meezy.bc.collaboration.team.application.service.dto.response.InviteCodeResponse;
import com.example.meezy.bc.collaboration.team.application.service.invite.InviteCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;

    @PostMapping("/{teamId}/invite-code")
    public InviteCodeResponse createInviteCode(@PathVariable UUID teamId) {
        return inviteCodeService.create(teamId);
    }

    @PostMapping("/join")
    public void join(@Valid @RequestBody TeamJoinRequest request) {
        inviteCodeService.join(request);
    }
}
