package com.example.meezy.bc.sharedkernel.file;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface AudioStoragePort {

    String uploadAudio(MultipartFile file);

    String uploadAudioFromPath(Path filePath);

    byte[] downloadAudio(String s3Key);

    void deleteAudio(String s3Key);
}
