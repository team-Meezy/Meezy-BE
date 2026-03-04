package com.example.meezy.bc.collaboration.meeting.application.service.dto.request;

import com.example.meezy.bc.sharedkernel.validation.NotEmptyMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public record ReceiveRecordingRequest(

        @NotEmptyMultipartFile
        MultipartFile file
) {
}
