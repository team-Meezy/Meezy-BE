package com.example.meezy.bc.collaboration.chat_message.application.service.dto.response;

import com.example.meezy.bc.collaboration.chat_message.domain.ChatMessage;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ChatMessageResponse(
        UUID chatMessageId,
        String senderName,
        String senderProfileImageUrl,
        String content,
        LocalDateTime createdAt
) {

    public static ChatMessageResponse from(ChatMessage chatMessage){
        return ChatMessageResponse.builder()
                .chatMessageId(chatMessage.getChatMessageId().value())
                .senderName(chatMessage.getSenderName())
                .senderProfileImageUrl(chatMessage.getSenderProfileImageUrl())
                .content(chatMessage.getContent())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }
}
