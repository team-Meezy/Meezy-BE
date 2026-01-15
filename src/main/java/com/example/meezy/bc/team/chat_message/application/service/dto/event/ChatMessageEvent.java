package com.example.meezy.bc.team.chat_message.application.service.dto.event;

import com.example.meezy.bc.team.chat_message.domain.ChatMessage;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChatMessageEvent(
        UUID chatRoomId,
        UUID chatMessageId,
        String senderName,
        String content,
        LocalDateTime createdAt
) {

    public static ChatMessageEvent from(ChatMessage chatMessage, UUID chatRoomId){
        return ChatMessageEvent.builder()
                .chatRoomId(chatRoomId)
                .chatMessageId(chatMessage.getChatMessageId().value())
                .senderName(chatMessage.getSenderName())
                .content(chatMessage.getContent())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
