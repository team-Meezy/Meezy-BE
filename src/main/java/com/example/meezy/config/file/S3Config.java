package com.example.meezy.config.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${cloud.file.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.file.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.file.garage.region}")
    private String region;

    @Value("${cloud.file.garage.endpoint}")
    private String endpoint;

    @Bean
    public S3Client s3Client(){
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey); //접근하기 위한 자격 증명 객체 생성
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint)) //Garage 엔드포인트로 요청을 보내도록 강제 변경
                .forcePathStyle(true)
                .build();
    }
}
