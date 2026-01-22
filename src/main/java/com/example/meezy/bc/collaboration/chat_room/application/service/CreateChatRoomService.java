package com.example.meezy.bc.collaboration.chat_room.application.service;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.collaboration.chat_room.application.service.dto.request.CreateChatRoomRequest;
import com.example.meezy.bc.collaboration.chat_room.domain.ChatRoom;
import com.example.meezy.bc.collaboration.chat_room.domain.repository.ChatRoomRepository;
import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreateChatRoomService {

    private final TeamRepository teamRepository; //같은 BC 간 참조 가능
    private final ChatRoomRepository chatRoomRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void create(UUID teamId, CreateChatRoomRequest request){

        Team team = findTeamOrThrow(teamId);

        team.validateLeaderPermission(currentUserQuery.currentUser().userId());

        chatRoomRepository.save(
                ChatRoom.create(team.getTeamId(), request.name())
        );
    }

    private Team findTeamOrThrow(UUID teamId) {
        return teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }
}
