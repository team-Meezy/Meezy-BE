package com.example.meezy.bc.collaboration.chat_room.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateChatRoomNameRequest(

        @NotBlank(message = "채팅방 이름을 입력해주세요.")
        @Size(max = 20, message = "채팅방은 최대 20글자까지 가능합니다.")
        String name
) {
}
