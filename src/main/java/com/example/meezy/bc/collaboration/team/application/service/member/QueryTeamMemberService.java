package com.example.meezy.bc.collaboration.team.application.service.member;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.collaboration.team.application.service.dto.response.TeamMemberResponse;
import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryTeamMemberService {

    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> query(UUID teamId) {
        Team team = teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);

        return team.getMembers().stream()
                .map(t -> TeamMemberResponse.from(t, currentUserQuery.currentUser().name()))
                .toList();
    }
}
