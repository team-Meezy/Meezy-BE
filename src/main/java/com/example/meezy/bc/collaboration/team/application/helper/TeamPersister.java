package com.example.meezy.bc.collaboration.team.application.helper;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeamPersister {

    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void save(String name, String imageUrl) {
        teamRepository.save(
                Team.create(name, imageUrl, currentUserQuery.currentUser().userId())
        );
    }

}
