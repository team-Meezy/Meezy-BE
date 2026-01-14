package com.example.meezy.bc.team.team.infrastructure.adapter.out;

import com.example.meezy.bc.team.team.application.port.out.FileStoragePort;
import com.example.meezy.bc.team.team.infrastructure.adapter.exception.FailedDeleteException;
import com.example.meezy.bc.team.team.infrastructure.adapter.exception.FailedUploadException;
import com.example.meezy.bc.team.team.infrastructure.adapter.exception.ImageNotFoundException;
import com.example.meezy.bc.team.team.infrastructure.adapter.exception.InvalidExtensionException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStoragePort {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png");

    private final S3Client s3Client;

    @Value("${cloud.file.garage.bucket}")
    private String bucket;

    @Value("${cloud.file.garage.url-prefix}")
    private String urlPrefix;

    @Override
    public String upload(MultipartFile file) {
        validateFile(file);

        String extension = extractExtension(file.getOriginalFilename());

        String key = generateSafeKey(extension);

        try(InputStream inputStream = file.getInputStream()){
            PutObjectRequest request = PutObjectRequest.builder() //업로드 요청 객체 생성
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );

            return urlPrefix + key;
        } catch (Exception  e) {
            throw new FailedUploadException();
        }
    }

    @Override
    public void deleteByKey(String key) {
        validateKey(key);

        try{
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw new FailedDeleteException();
        }
    }

    @Override
    public String update(String oldKey, MultipartFile newFile) {
        if(newFile == null || newFile.isEmpty()){
            return oldKey;
        }

        String newUrl = upload(newFile);

        if(oldKey != null){
            try {
                deleteByKey(oldKey);
            } catch (Exception e){
                //기존 파일 삭제 실패해도 신규 업로드는 유지
            }
        }

        return newUrl;
    }

    private void validateFile(MultipartFile file){
        if (file == null || file.isEmpty()) {
            throw new ImageNotFoundException();
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidExtensionException();
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidExtensionException();
        }
    }

    private void validateKey(String key) {
        if (key == null || key.contains("..") || key.contains("/")) {
            throw new FailedDeleteException();
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new InvalidExtensionException();
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(); //확장자 반환
    }

    private String generateSafeKey(String extension) {
        return UUID.randomUUID() + "." + extension;
    }
}
