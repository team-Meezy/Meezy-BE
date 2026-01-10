package com.example.meezy.bc.user.infrastructure.security.oauth.parse.kako;

import com.example.meezy.bc.user.domain.type.OauthProvider;
import com.example.meezy.bc.user.infrastructure.security.oauth.dto.OauthUserInfo;
import com.example.meezy.bc.user.infrastructure.security.oauth.parse.OauthUserInfoParser;

import java.util.Map;

public class KakaoOAuthUserInfoParser implements OauthUserInfoParser {

    @Override
    public OauthUserInfo parse(Map<String, Object> attributes, OauthProvider provider) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) account.get("email");
        return new OauthUserInfo(email, provider, attributes);
    }
}
