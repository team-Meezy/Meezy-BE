package com.example.meezy.bc.team.chat_message.infrastructure.adapter.out.messaging;

import com.example.meezy.bc.team.chat_message.application.port.out.ChatMessagePublishPort;
import com.example.meezy.bc.team.chat_message.application.service.dto.event.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBrokerPublisher implements ChatMessagePublishPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public CompletableFuture<Boolean> publish(ChatMessageEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            try{
                rabbitTemplate.convertAndSend(event);
                return true;
            } catch (Exception e){
                log.error("메시지 전송에 실패: {}", e.getMessage(), e);
                return false;
            }
        });
    }
}
