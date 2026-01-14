package com.example.meezy.bc.team.team.presentation.controller;

import com.example.meezy.bc.team.team.application.service.dto.response.TeamMemberResponse;
import com.example.meezy.bc.team.team.application.service.member.DeleteTeamMemberService;
import com.example.meezy.bc.team.team.application.service.member.QueryTeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams/{teamId}/members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final QueryTeamMemberService queryTeamMemberService;
    private final DeleteTeamMemberService deleteTeamMemberService;

    @GetMapping
    public List<TeamMemberResponse> queryMembers(@PathVariable UUID teamId) {
        return queryTeamMemberService.query(teamId);
    }

    @DeleteMapping("/{memberId}")
    public void kick(
            @PathVariable UUID teamId,
            @PathVariable UUID memberId
    ) {
        deleteTeamMemberService.kick(teamId, memberId);
    }
}
