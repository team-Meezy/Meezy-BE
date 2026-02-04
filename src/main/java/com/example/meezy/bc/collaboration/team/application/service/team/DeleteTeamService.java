package com.example.meezy.bc.collaboration.team.application.service.team;

import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.event.TeamDeletedEvent;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteTeamService {

    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void delete(UUID teamId) {
        Team team = teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);

        team.validateCanDelete(currentUserQuery.currentUser().userId());

        String serverImageUrl = team.getServerImageUrl();
        teamRepository.delete(team);

        eventPublisher.publishEvent(new TeamDeletedEvent(serverImageUrl));
    }
}
