package com.example.meezy.bc.team.team.application.service.invite;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.team.application.service.dto.request.TeamJoinRequest;
import com.example.meezy.bc.team.team.application.service.dto.response.InviteCodeResponse;
import com.example.meezy.bc.team.team.domain.Team;
import com.example.meezy.bc.team.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.team.team.domain.exception.InvalidInviteCodeException;
import com.example.meezy.bc.team.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InviteCodeService {

    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public InviteCodeResponse create(UUID teamId){
        Team team = teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);

        return InviteCodeResponse.from(team.generateInviteCode(currentUserQuery.currentUser().userId()));
    }

    @Transactional
    public void join(TeamJoinRequest request){
        Team team = teamRepository.findByInviteCode_Code(request.inviteCode())
                .orElseThrow(InvalidInviteCodeException::new);

        team.joinByInviteCode(request.inviteCode(), currentUserQuery.currentUser().userId());
    }
}
