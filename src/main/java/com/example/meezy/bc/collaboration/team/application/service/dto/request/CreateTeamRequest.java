package com.example.meezy.bc.collaboration.team.application.service.dto.request;

import com.example.meezy.bc.sharedkernel.validation.NotEmptyMultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record CreateTeamRequest(

        @NotBlank(message = "팀 이름을 입력해주세요.")
        @Size(max = 20, message = "팀 이름은 최대 20자까지 작성할 수 있습니다.")
        String name,

        @NotEmptyMultipartFile
        MultipartFile serverImage
) {
}
