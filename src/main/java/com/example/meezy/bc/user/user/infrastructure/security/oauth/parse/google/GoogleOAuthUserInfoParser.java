package com.example.meezy.bc.user.user.infrastructure.security.oauth.parse.google;

import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.dto.OauthUserInfo;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.parse.OauthUserInfoParser;

import java.util.Map;

public class GoogleOAuthUserInfoParser implements OauthUserInfoParser {

    @Override
    public OauthUserInfo parse(Map<String, Object> attributes, OauthProvider provider) {
        String email = (String) attributes.get("email");
        return new OauthUserInfo(email, provider, attributes);
    }
}
