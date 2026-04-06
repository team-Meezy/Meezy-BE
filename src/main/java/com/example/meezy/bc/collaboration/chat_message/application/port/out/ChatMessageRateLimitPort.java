package com.example.meezy.bc.collaboration.chat_message.application.port.out;

import java.util.UUID;

public interface ChatMessageRateLimitPort {

    void validate(UUID userId, UUID chatRoomId);
}
