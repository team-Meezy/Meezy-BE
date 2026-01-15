package com.example.meezy.bc.team.chat_room.application.service.dto.response;

import com.example.meezy.bc.team.chat_room.domain.ChatRoom;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ChatRoomResponse(
        UUID chatRoomId,
        String name
) {

    public static ChatRoomResponse from(ChatRoom chatRoom){
        return ChatRoomResponse.builder()
                .chatRoomId(chatRoom.getChatRoomId().value())
                .name(chatRoom.getName())
                .build();
    }
}
