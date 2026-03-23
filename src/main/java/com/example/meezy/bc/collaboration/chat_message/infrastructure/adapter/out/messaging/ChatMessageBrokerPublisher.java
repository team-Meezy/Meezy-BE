package com.example.meezy.bc.collaboration.chat_message.infrastructure.adapter.out.messaging;

import com.example.meezy.bc.collaboration.chat_message.application.port.out.ChatMessagePublishPort;
import com.example.meezy.bc.collaboration.chat_message.application.service.dto.event.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBrokerPublisher implements ChatMessagePublishPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public boolean publish(ChatMessageEvent event) {
        try {
            rabbitTemplate.convertAndSend(event);
            return true;
        } catch (Exception e) {
            log.error("메시지 전송에 실패: {}", e.getMessage(), e);
            return false;
        }
    }
}
