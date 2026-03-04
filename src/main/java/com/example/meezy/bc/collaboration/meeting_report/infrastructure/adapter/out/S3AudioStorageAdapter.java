package com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out;

import com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out.exception.AudioDownloadFailedException;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out.exception.AudioUploadFailedException;
import com.example.meezy.bc.sharedkernel.file.AudioStoragePort;
import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3AudioStorageAdapter implements AudioStoragePort {

    private static final String KEY_PREFIX = "recordings/";

    private final S3Client s3Client;

    @Value("${cloud.file.garage.bucket}")
    private String bucket;

    @Override
    public String uploadAudio(MultipartFile file) {
        String key = KEY_PREFIX + UUID.randomUUID() + ".mp3";

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
            log.error("오디오 파일 업로드 실패: ", e);
            throw new AudioUploadFailedException();
        }
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
