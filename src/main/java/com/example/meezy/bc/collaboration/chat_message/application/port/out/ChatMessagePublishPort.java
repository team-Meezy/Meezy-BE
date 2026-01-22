package com.example.meezy.bc.collaboration.chat_message.application.port.out;

import com.example.meezy.bc.collaboration.chat_message.application.service.dto.event.ChatMessageEvent;

import java.util.concurrent.CompletableFuture;

public interface ChatMessagePublishPort {
    CompletableFuture<Boolean> publish(ChatMessageEvent event); //비동기로 반환되는 값 담기
}
