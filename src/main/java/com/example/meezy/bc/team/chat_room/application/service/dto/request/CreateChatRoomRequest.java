package com.example.meezy.bc.team.chat_room.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChatRoomRequest(

        @NotBlank(message = "채팅방 이름을 입력해주세요.")
        @Size(max = 10)
        String name
) {
}
