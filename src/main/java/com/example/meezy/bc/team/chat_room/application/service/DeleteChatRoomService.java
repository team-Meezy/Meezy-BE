package com.example.meezy.bc.team.chat_room.application.service;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.chat_room.application.service.exception.ChatRoomNotFoundException;
import com.example.meezy.bc.team.chat_room.domain.ChatRoom;
import com.example.meezy.bc.team.chat_room.domain.repository.ChatRoomRepository;
import com.example.meezy.bc.team.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.team.team.domain.Team;
import com.example.meezy.bc.team.team.domain.repository.TeamRepository;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteChatRoomService {

    private final TeamRepository teamRepository; //같은 BC 간 참조 가능
    private final ChatRoomRepository chatRoomRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public void delete(UUID teamId, UUID chatRoomId){
        Team team = findTeamOrThrow(teamId);
        ChatRoom chatRoom = findChatRoomOrThrow(chatRoomId);

        validateTeamOwnership(chatRoom, teamId);
        team.validateLeaderPermission(currentUserQuery.currentUser().userId());

        chatRoomRepository.delete(chatRoom);
    }

    private Team findTeamOrThrow(UUID teamId) {
        return teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    private ChatRoom findChatRoomOrThrow(UUID chatRoomId){
        return chatRoomRepository.findByChatRoomId_Value(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);
    }

    private void validateTeamOwnership(ChatRoom chatRoom, UUID teamId) {
        if (!chatRoom.getTeamId().equals(TeamId.of(teamId))) {
            throw new ChatRoomNotFoundException();
        }
    }
}
