package com.example.meezy.bc.team.team.application.service.team;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.team.application.port.out.FileStoragePort;
import com.example.meezy.bc.team.team.application.service.dto.request.CreateTeamRequest;
import com.example.meezy.bc.team.team.domain.Team;
import com.example.meezy.bc.team.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateTeamService {

    private final CurrentUserQuery currentUserQuery;
    private final TeamRepository teamRepository;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public void create(CreateTeamRequest request){
        teamRepository.save(
                Team.create(
                        request.name(),
                        fileStoragePort.upload(request.serverImage()),
                        currentUserQuery.currentUser().userId()
                )
        );
    }
}
