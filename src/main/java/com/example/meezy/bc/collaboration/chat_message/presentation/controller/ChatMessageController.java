package com.example.meezy.bc.collaboration.chat_message.presentation.controller;

import com.example.meezy.bc.collaboration.chat_message.application.service.QueryChatMessageService;
import com.example.meezy.bc.collaboration.chat_message.application.service.SendChatMessageService;
import com.example.meezy.bc.collaboration.chat_message.application.service.dto.request.ChatMessageRequest;
import com.example.meezy.bc.collaboration.chat_message.application.service.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SendChatMessageService sendChatMessageService;
    private final QueryChatMessageService queryChatMessageService;

    @MessageMapping("/teams/{teamId}/chat-rooms/{chatRoomId}/messages")
    public void send(
            @DestinationVariable UUID teamId,
            @DestinationVariable UUID chatRoomId,
            @Payload ChatMessageRequest request
    ) {
        sendChatMessageService.send(teamId, chatRoomId, request);
    }

    @GetMapping("/teams/{teamId}/chat-rooms/{chatRoomId}/messages")
    @ResponseBody
    public List<ChatMessageResponse> findAll(
            @PathVariable UUID teamId,
            @PathVariable UUID chatRoomId
    ) {
        return queryChatMessageService.findAll(teamId, chatRoomId);
    }
}
