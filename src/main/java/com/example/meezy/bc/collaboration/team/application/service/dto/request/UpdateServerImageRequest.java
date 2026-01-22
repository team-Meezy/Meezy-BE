package com.example.meezy.bc.collaboration.team.application.service.dto.request;

import com.example.meezy.bc.collaboration.team.application.validation.NotEmptyMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public record UpdateServerImageRequest(

        @NotEmptyMultipartFile
        MultipartFile serverImage
) {
}
