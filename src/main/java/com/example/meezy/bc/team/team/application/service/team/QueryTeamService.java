package com.example.meezy.bc.team.team.application.service.team;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.team.application.service.dto.response.TeamResponse;
import com.example.meezy.bc.team.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.team.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryTeamService {

    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public TeamResponse query(UUID teamId){
        return teamRepository.findByTeamId_Value(teamId)
                .map(TeamResponse::from)
                .orElseThrow(TeamNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> queryAll(){
        return teamRepository.findTeamsByUserIdWithMembers(currentUserQuery.currentUser().userId()).stream()
                .map(TeamResponse::from)
                .toList();
    }
}
