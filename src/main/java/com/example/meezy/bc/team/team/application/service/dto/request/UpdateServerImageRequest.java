package com.example.meezy.bc.team.team.application.service.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UpdateServerImageRequest(
        @NotNull(message = "이미지는 필수입니다.")
        MultipartFile serverImage
) {
}
