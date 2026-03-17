package com.example.meezy.bc.collaboration.team.application.service.member;

import com.example.meezy.bc.collaboration.team.application.service.dto.response.TeamMemberResponse;
import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryTeamMemberService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> query(UUID teamId) {
        Team team = teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);

        List<UUID> userIds = team.getMembers().stream()
                .map(m -> m.getUserId().value())
                .toList();

        List<User> users = userRepository.findByUserId_ValueIn(userIds);

        Map<UUID, String> nameMap = users.stream()
                .collect(Collectors.toMap(
                        u -> u.getUserId().value(),
                        User::getName
                ));

        Map<UUID, String> profileImageMap = users.stream()
                .collect(Collectors.toMap(
                        u -> u.getUserId().value(),
                        u -> u.getProfileImageUrl() != null ? u.getProfileImageUrl() : ""
                ));

        return team.getMembers().stream()
                .map(m -> TeamMemberResponse.from(
                        m,
                        nameMap.get(m.getUserId().value()),
                        profileImageMap.get(m.getUserId().value())
                ))
                .toList();
    }
}
