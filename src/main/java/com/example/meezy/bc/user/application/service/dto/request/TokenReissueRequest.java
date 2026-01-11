package com.example.meezy.bc.user.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(

        @NotBlank(message = "리프레시 토큰을 입력해주세요")
        String refreshToken
) {
}
