package com.example.meezy.bc.collaboration.chat_room.application.service;

import com.example.meezy.bc.collaboration.chat_room.application.service.dto.response.ChatRoomResponse;
import com.example.meezy.bc.collaboration.chat_room.domain.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> queryAll(UUID teamId){
        return chatRoomRepository.findAllByTeamId_Value(teamId).stream()
                .map(ChatRoomResponse::from)
                .toList();
    }
}
