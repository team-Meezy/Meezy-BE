package com.example.meezy.bc.collaboration.meeting.application.service.dto.request;

import com.example.meezy.bc.sharedkernel.validation.NotEmptyMultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record ReceiveRecordingRequest(

        @NotEmptyMultipartFile
        MultipartFile file,

        @NotBlank(message = "회의 제목을 입력해주세요.")
        @Size(max = 100, message = "회의 제목은 최대 100자까지 입력해주세요.")
        String title
) {
}
