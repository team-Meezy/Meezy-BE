package com.example.meezy.bc.collaboration.team.infrastructure.adapter.out;

import com.example.meezy.bc.collaboration.team.application.port.out.FileStoragePort;
import com.example.meezy.bc.collaboration.team.infrastructure.adapter.exception.FailedDeleteException;
import com.example.meezy.bc.collaboration.team.infrastructure.adapter.exception.FailedUploadException;
import com.example.meezy.bc.collaboration.team.infrastructure.adapter.exception.ImageNotFoundException;
import com.example.meezy.bc.collaboration.team.infrastructure.adapter.exception.InvalidExtensionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class S3FileStorageAdapter implements FileStoragePort {

    private static final String KEY_PREFIX = "serverProfile";

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

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(inputStream, file.getSize())
            );

            return buildPublicUrl(key);

        } catch (Exception e) {
            log.error("파일 업로드에 실패했습니다. : ", e);
            throw new FailedUploadException();
        }
    }

    @Override
    public void deleteByKey(String url) {
        String key = extractKeyFromUrl(url);
        validateKey(key);

        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.error("파일 삭제에 실패했습니다. : ", e);
            throw new FailedDeleteException();
        }
    }

    @Override
    public String update(String oldUrl, MultipartFile newFile) {
        if (newFile == null || newFile.isEmpty()) {
            return oldUrl;
        }

        String newUrl = upload(newFile);

        if (oldUrl != null) {
            try {
                deleteByKey(oldUrl);
            } catch (Exception e) {
                log.warn("삭제 실패, 고아 파일 발생 가능: {}", oldUrl, e);
            }
        }

        return newUrl;
    }

    private String buildPublicUrl(String key) {
        return urlPrefix.endsWith("/")
                ? urlPrefix + key
                : urlPrefix + "/" + key;
    }

    private String extractKeyFromUrl(String url) {
        try {
            var uri = java.net.URI.create(url);
            String path = uri.getPath();

            if (path == null || path.length() <= 1) {
                throw new FailedDeleteException();
            }

            return path.substring(1); // 앞의 '/' 제거
        } catch (Exception e) {
            throw new FailedDeleteException();
        }
    }

    private void validateFile(MultipartFile file) {
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
        if (key == null || key.isBlank() || key.contains("..") || key.contains("/")) {
            throw new FailedDeleteException();
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new InvalidExtensionException();
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String generateSafeKey(String extension) {
        return KEY_PREFIX + UUID.randomUUID() + "." + extension;
    }
}
