package com.example.meezy.bc.sharedkernel.file;

import org.springframework.web.multipart.MultipartFile;

public interface AudioStoragePort {

    String uploadAudio(MultipartFile file);

    byte[] downloadAudio(String s3Key);

    void deleteAudio(String s3Key);
}
