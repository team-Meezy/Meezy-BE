package com.example.meezy.bc.collaboration.team.application.service.member;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteTeamMemberService {

    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void kick(UUID teamId, UUID teamMemberId){
        Team team = teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);

        team.removeMember(
                currentUserQuery.currentUser().userId(),
                TeamMemberId.of(teamMemberId)
        );
    }
}
