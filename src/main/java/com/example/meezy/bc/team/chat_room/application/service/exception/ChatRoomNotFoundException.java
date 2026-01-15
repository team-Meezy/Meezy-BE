package com.example.meezy.bc.team.chat_room.application.service.exception;

import com.example.meezy.bc.sharedkernel.exception.CustomException;
import com.example.meezy.bc.sharedkernel.exception.ErrorCode;

public class ChatRoomNotFoundException extends CustomException {

    public ChatRoomNotFoundException() {
        super(ErrorCode.CHAT_ROOM_NOT_FOUND);
    }
}
