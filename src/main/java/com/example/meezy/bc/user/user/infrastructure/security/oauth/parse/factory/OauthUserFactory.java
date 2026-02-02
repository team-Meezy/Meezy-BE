package com.example.meezy.bc.user.user.infrastructure.security.oauth.parse.factory;

import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.dto.OauthUserInfo;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.parse.google.GoogleOAuthUserInfoParser;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.parse.kako.KakaoOAuthUserInfoParser;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.parse.naver.NaverOAuthUserInfoParser;

import java.util.Map;

public class OauthUserFactory {

    public static OauthUserInfo getParser(OauthProvider provider, Map<String, Object> attributes){
        return switch (provider){
            case GOOGLE -> new GoogleOAuthUserInfoParser().parse(attributes, provider);
            case KAKAO -> new KakaoOAuthUserInfoParser().parse(attributes, provider);
            case NAVER -> new NaverOAuthUserInfoParser().parse(attributes, provider);
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자: " + provider);
        };
    }
}
