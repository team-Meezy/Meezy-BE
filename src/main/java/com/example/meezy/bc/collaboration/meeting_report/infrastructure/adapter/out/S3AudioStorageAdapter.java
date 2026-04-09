package com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out;

import com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out.exception.AudioDownloadFailedException;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out.exception.AudioUploadFailedException;
import com.example.meezy.bc.sharedkernel.file.AudioStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
public class S3AudioStorageAdapter implements AudioStoragePort {

    private static final String KEY_PREFIX = "recordings/";
    private static final int MAX_CONCURRENT_UPLOADS = 5;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_BASE_DELAY_MS = 500;

    private final S3Client s3Client;
    private final Semaphore uploadSemaphore = new Semaphore(MAX_CONCURRENT_UPLOADS);

    @Value("${cloud.file.garage.bucket}")
    private String bucket;

    public S3AudioStorageAdapter(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String uploadAudio(MultipartFile file) {
        String key = KEY_PREFIX + UUID.randomUUID() + ".mp3";

        try {
            uploadSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AudioUploadFailedException();
        }

        try {
            return uploadWithRetry(file, key);
        } finally {
            uploadSemaphore.release();
        }
    }

    private String uploadWithRetry(MultipartFile file, String key) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(file.getContentType())
                        .contentLength(file.getSize())
                        .build();

                s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
                return key;
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) {
                    log.error("오디오 파일 업로드 최종 실패 ({}회 시도): ", MAX_RETRIES, e);
                    throw new AudioUploadFailedException();
                }
                long delay = RETRY_BASE_DELAY_MS * attempt;
                log.warn("오디오 파일 업로드 실패 (시도 {}/{}), {}ms 후 재시도", attempt, MAX_RETRIES, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AudioUploadFailedException();
                }
            }
        }
        throw new AudioUploadFailedException();
    }

    @Override
    public String uploadAudioFromPath(Path filePath) {
        String key = KEY_PREFIX + UUID.randomUUID() + ".mp3";

        try {
            uploadSemaphore.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AudioUploadFailedException();
        }

        try {
            return uploadFromPathWithRetry(filePath, key);
        } finally {
            uploadSemaphore.release();
        }
    }

    private String uploadFromPathWithRetry(Path filePath, String key) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                long fileSize = Files.size(filePath);
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("audio/mpeg")
                        .contentLength(fileSize)
                        .build();

                s3Client.putObject(request, RequestBody.fromFile(filePath));
                return key;
            } catch (Exception e) {
                if (attempt == MAX_RETRIES) {
                    log.error("오디오 파일 업로드 최종 실패 ({}회 시도): ", MAX_RETRIES, e);
                    throw new AudioUploadFailedException();
                }
                long delay = RETRY_BASE_DELAY_MS * attempt;
                log.warn("오디오 파일 업로드 실패 (시도 {}/{}), {}ms 후 재시도", attempt, MAX_RETRIES, delay);
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new AudioUploadFailedException();
                }
            }
        }
        throw new AudioUploadFailedException();
    }

    @Override
    public byte[] downloadAudio(String s3Key) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
                return response.readAllBytes();
            }
        } catch (Exception e) {
            log.error("오디오 파일 다운로드 실패: key={}", s3Key, e);
            throw new AudioDownloadFailedException();
        }
    }

    @Override
    public Path downloadAudioToTempFile(String s3Key) {
        try {
            String extension = s3Key.endsWith(".mp3") ? ".mp3" : ".audio";
            Path tempFile = Files.createTempFile("recording-download-", extension);

            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();

            try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
                Files.copy(response, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            return tempFile;
        } catch (Exception e) {
            log.error("오디오 파일 다운로드 실패: key={}", s3Key, e);
            throw new AudioDownloadFailedException();
        }
    }

    @Override
    public void deleteAudio(String s3Key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(request);
        } catch (Exception e) {
            log.warn("오디오 파일 삭제 실패 (고아 파일 발생 가능): key={}", s3Key, e);
        }
    }
}
