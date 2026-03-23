package com.example.meezy.bc.collaboration.chat_message.application.port.out;

import com.example.meezy.bc.collaboration.chat_message.application.service.dto.event.ChatMessageEvent;

public interface ChatMessagePublishPort {
    boolean publish(ChatMessageEvent event);
}
