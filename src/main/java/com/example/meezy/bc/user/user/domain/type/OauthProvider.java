package com.example.meezy.bc.user.user.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OauthProvider {
    GOOGLE("google"),
    KAKAO("kakao"),
    NAVER("naver"),
    LOCAL("local");

    private final String registrationId;
}
