package com.example.meezy.bc.user.infrastructure.security.email;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Builder
@RedisHash("email_verification_attempt")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmailVerificationAttempt {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_TTL_SECONDS = 24 * 60 * 60; // 24시간

    @Id
    private String email;

    private int attemptCount; //틀린 횟수 카운트

    @TimeToLive
    private Long ttl;

    //아무 인증도 하지 않았을 때 생성
    public static EmailVerificationAttempt createNew(String email) {
        return EmailVerificationAttempt.builder()
                .email(email)
                .attemptCount(1)
                .ttl(BLOCK_TTL_SECONDS)
                .build();
    }

    //실패 누적
    public EmailVerificationAttempt incrementAttempt() {//실패할 때마다
        return EmailVerificationAttempt.builder()
                .email(this.email)
                .attemptCount(this.attemptCount + 1)
                .ttl(BLOCK_TTL_SECONDS)
                .build();
    }

    public boolean isBlocked() {
        return this.attemptCount >= MAX_ATTEMPTS;
    }

    public int getRemainingAttempts() {
        return Math.max(0, MAX_ATTEMPTS - this.attemptCount);
    }
}
