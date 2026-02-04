package com.example.meezy.bc.user.user.application.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        @Size(min = 8, max = 30, message = "비밀번호는 8~30자여야 합니다.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Size(min = 8, max = 30, message = "비밀번호는 8~30자여야 합니다.")
        String newPassword
) {
}
