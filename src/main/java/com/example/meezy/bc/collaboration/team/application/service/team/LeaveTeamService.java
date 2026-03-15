package com.example.meezy.bc.collaboration.team.application.service.team;

import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.event.TeamMemberLeftEvent;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveTeamService {

    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void leave(UUID teamId) {
        Team team = teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);

        var userId = currentUserQuery.currentUser().userId();
        team.leaveTeam(userId);

        eventPublisher.publishEvent(new TeamMemberLeftEvent(teamId, userId.value()));
    }
}
