package com.example.meezy.bc.collaboration.chat_message.domain.vo;

import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record ChatMessageId(
        UUID value
) {

    public static ChatMessageId newId(){
        return new ChatMessageId(UUID.randomUUID());
    }

    public static ChatMessageId of(UUID chatMessageId){
        return new ChatMessageId(chatMessageId);
    }
}
