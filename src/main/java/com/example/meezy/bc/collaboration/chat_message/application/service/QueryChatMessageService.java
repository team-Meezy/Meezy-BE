package com.example.meezy.bc.collaboration.chat_message.application.service;

import com.example.meezy.bc.collaboration.chat_message.application.service.dto.response.ChatMessageResponse;
import com.example.meezy.bc.collaboration.chat_message.domain.repository.ChatMessageRepository;
import com.example.meezy.bc.collaboration.chat_room.application.service.exception.ChatRoomNotFoundException;
import com.example.meezy.bc.collaboration.chat_room.domain.ChatRoom;
import com.example.meezy.bc.collaboration.chat_room.domain.repository.ChatRoomRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> findAll(UUID teamId, UUID chatRoomId){

        ChatRoom chatRoom = chatRoomRepository.findByChatRoomId_Value(chatRoomId)
                .orElseThrow(ChatRoomNotFoundException::new);

        validateTeamOwnership(chatRoom, teamId);

        return chatMessageRepository.findAllByChatRoomIdOrderByCreatedAtAscChatMessageIdAsc(chatRoom.getChatRoomId()).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    private void validateTeamOwnership(ChatRoom chatRoom, UUID teamId){
        if (!chatRoom.getTeamId().equals(TeamId.of(teamId))) {
            throw new ChatRoomNotFoundException();
        }
    }
}
