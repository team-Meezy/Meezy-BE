package com.example.meezy.bc.user.user.infrastructure.security.oauth.parse.naver;

import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.dto.OauthUserInfo;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.parse.OauthUserInfoParser;

import java.util.Map;

public class NaverOAuthUserInfoParser implements OauthUserInfoParser {

    @Override
    public OauthUserInfo parse(Map<String, Object> attributes, OauthProvider provider) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        String email = (String) response.get("email");
        return new OauthUserInfo(email, provider, attributes);
    }
}
