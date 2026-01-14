package com.example.meezy.bc.team.team.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTeamNameRequest(
        @NotBlank(message = "팀 이름을 입력해주세요.")
        @Size(max = 20, message = "팀 이름은 최대 20자까지 작성할 수 있습니다.")
        String name
) {
}
