package com.example.meezy.bc.collaboration.team.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TeamJoinRequest(

        @NotBlank(message = "초대 코드를 입력해 주세요.")
        @Size(max = 9)
        String inviteCode
) {
}
