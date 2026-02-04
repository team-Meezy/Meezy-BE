package com.example.meezy.bc.collaboration.team.application.service.team;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.sharedkernel.file.FileStoragePort;
import com.example.meezy.bc.collaboration.team.application.service.dto.request.UpdateServerImageRequest;
import com.example.meezy.bc.collaboration.team.application.service.dto.request.UpdateTeamNameRequest;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateTeamService {

    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public void updateName(UUID teamId, UpdateTeamNameRequest request){
        Team team = teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);

        team.changeName(currentUserQuery.currentUser().userId(), request.name());
    }

    @Transactional
    public void updateServerImage(UUID teamId, UpdateServerImageRequest request){
        Team team = teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);

        team.changeServerImage(
                currentUserQuery.currentUser().userId(),
                fileStoragePort.update(team.getServerImageUrl(), request.serverImage())
        );
    }
}
