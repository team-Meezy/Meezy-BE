package com.example.meezy.bc.collaboration.team.application.port.out;

import org.springframework.web.multipart.MultipartFile;

public interface FileStoragePort {

    String upload(MultipartFile file);
    void deleteByKey(String key);
    String update(String oldKey, MultipartFile newFile);
}
