package com.example.meezy.bc.user.user.infrastructure.security.email;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

//인증을 완료한 상태를 표시
@Getter
@Builder
@RedisHash("verified_email")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VerifiedEmail {

    private static final long DEFAULT_TTL_SECONDS = 30 * 60; // 30분 -> 영구적으로 값을 저장하지 않기 위해

    @Id
    private String email;

    @TimeToLive
    private Long ttl;

    public static VerifiedEmail of(String email) {
        return VerifiedEmail.builder()
                .email(email)
                .ttl(DEFAULT_TTL_SECONDS)
                .build();
    }
}
