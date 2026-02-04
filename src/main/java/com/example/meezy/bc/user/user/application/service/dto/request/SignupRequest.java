package com.example.meezy.bc.user.user.application.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        @Size(max = 255, message = "255자 이내로 작성해주세요.")
        String email,

        @NotBlank(message = "accountId를 입력해주세요.")
        @Size(min = 5, max = 50, message = "accountId는 5 ~ 50 사이로 입력해주세요.")
        String accountId,

        @NotBlank(message = "이름은 필수입니다.")
        @Size(min =3, max = 100, message = "이름을 5 ~ 50 사이로 입력해주세요.")
        String name,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, max = 30, message = "비밀번호는 8~30자여야 합니다.")
        String password
) {}

