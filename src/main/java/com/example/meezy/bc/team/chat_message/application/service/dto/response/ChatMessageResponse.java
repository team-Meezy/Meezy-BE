package com.example.meezy.bc.team.chat_message.application.service.dto.response;

import com.example.meezy.bc.team.chat_message.domain.ChatMessage;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChatMessageResponse(
        UUID chatMessageId,
        String senderName,
        String content,
        LocalDateTime createdAt
) {

    public static ChatMessageResponse from(ChatMessage chatMessage){
        return ChatMessageResponse.builder()
                .chatMessageId(chatMessage.getChatMessageId().value())
                .senderName(chatMessage.getSenderName())
                .content(chatMessage.getContent())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
