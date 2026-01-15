package com.example.meezy.bc.team.chat_message.infrastructure.adapter.out.messaging;

import com.example.meezy.bc.team.chat_message.application.service.dto.event.ChatMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageBrokerListener {

    private static final String DESTINATION_PREFIX = "/topic/chat/";
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "#{chatMessageQueue.name}", containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(ChatMessageEvent event) {
        try {
            messagingTemplate.convertAndSend(DESTINATION_PREFIX + event.chatRoomId(), event);
        } catch (Exception e) {
            log.error("웹 소켓 메시지 전송 실패: roomId={}, error={}",
                    event.chatRoomId(), e.getMessage(), e);
        }
    }

}
