package com.example.meezy.bc.team.team.application.service.team;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.team.application.port.out.FileStoragePort;
import com.example.meezy.bc.team.team.domain.Team;
import com.example.meezy.bc.team.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.team.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteTeamService {

    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public void delete(UUID teamId){
        Team team = teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);

        team.validateCanDelete(currentUserQuery.currentUser().userId());

        fileStoragePort.deleteByKey(team.getServerImageUrl());

        teamRepository.delete(team);
    }
}
