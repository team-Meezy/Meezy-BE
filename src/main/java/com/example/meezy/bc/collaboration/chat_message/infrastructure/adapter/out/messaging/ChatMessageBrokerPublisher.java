package com.example.meezy.bc.collaboration.chat_message.infrastructure.adapter.out.messaging;

import com.example.meezy.bc.collaboration.chat_message.application.port.out.ChatMessagePublishPort;
import com.example.meezy.bc.collaboration.chat_message.application.service.dto.event.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBrokerPublisher implements ChatMessagePublishPort {

    private static final ExecutorService VIRTUAL_THREAD_EXECUTOR =
            Executors.newVirtualThreadPerTaskExecutor();

    private final RabbitTemplate rabbitTemplate;

    @Override
    public CompletableFuture<Boolean> publish(ChatMessageEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                rabbitTemplate.convertAndSend(event);
                return true;
            } catch (Exception e) {
                log.error("메시지 전송에 실패: {}", e.getMessage(), e);
                return false;
            }
        }, VIRTUAL_THREAD_EXECUTOR);
    }
}
