package com.example.meezy.bc.team.team.presentation.controller;

import com.example.meezy.bc.team.team.application.service.dto.request.CreateTeamRequest;
import com.example.meezy.bc.team.team.application.service.dto.request.UpdateServerImageRequest;
import com.example.meezy.bc.team.team.application.service.dto.request.UpdateTeamNameRequest;
import com.example.meezy.bc.team.team.application.service.dto.response.TeamResponse;
import com.example.meezy.bc.team.team.application.service.team.CreateTeamService;
import com.example.meezy.bc.team.team.application.service.team.DeleteTeamService;
import com.example.meezy.bc.team.team.application.service.team.QueryTeamService;
import com.example.meezy.bc.team.team.application.service.team.UpdateTeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final CreateTeamService createTeamService;
    private final UpdateTeamService updateTeamService;
    private final DeleteTeamService deleteTeamService;
    private final QueryTeamService queryTeamService;

    @PostMapping
    public void create(@Valid @ModelAttribute CreateTeamRequest request) {
        createTeamService.create(request);
    }

    @GetMapping("/{teamId}")
    public TeamResponse query(@PathVariable UUID teamId) {
        return queryTeamService.query(teamId);
    }

    @GetMapping
    public List<TeamResponse> queryAll() {
        return queryTeamService.queryAll();
    }

    @PatchMapping("/{teamId}/name")
    public void updateName(
            @PathVariable UUID teamId,
            @Valid @RequestBody UpdateTeamNameRequest request
    ) {
        updateTeamService.updateName(teamId, request);
    }

    @PatchMapping("/{teamId}/image")
    public void updateServerImage(
            @PathVariable UUID teamId,
            @Valid @ModelAttribute UpdateServerImageRequest request
    ) {
        updateTeamService.updateServerImage(teamId, request);
    }

    @DeleteMapping("/{teamId}")
    public void delete(@PathVariable UUID teamId) {
        deleteTeamService.delete(teamId);
    }
}
