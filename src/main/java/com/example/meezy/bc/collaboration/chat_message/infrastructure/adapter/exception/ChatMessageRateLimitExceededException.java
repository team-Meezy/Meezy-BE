package com.example.meezy.bc.collaboration.chat_message.infrastructure.adapter.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class ChatMessageRateLimitExceededException extends CustomException {

    public ChatMessageRateLimitExceededException() {
        super(ErrorCode.CHAT_MESSAGE_RATE_LIMIT_EXCEEDED);
    }
}
