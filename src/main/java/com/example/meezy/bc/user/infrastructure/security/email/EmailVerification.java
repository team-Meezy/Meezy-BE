package com.example.meezy.bc.user.infrastructure.security.email;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

//인증 코드를 저장하는 역할을 수행
@Getter
@Builder
@RedisHash("email_verification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class EmailVerification {

    @Id
    private String email;

    @Indexed
    private String code;

    @TimeToLive
    private Long ttl;
}
