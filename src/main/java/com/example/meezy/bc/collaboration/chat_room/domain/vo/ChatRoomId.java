package com.example.meezy.bc.collaboration.chat_room.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ChatRoomId(
        UUID value
) {

    public static ChatRoomId newId(){
        return new ChatRoomId(UUID.randomUUID());
    }

    public static ChatRoomId of(UUID chatRoomId){
        return new ChatRoomId(chatRoomId);
    }
}
