package com.example.meezy.bc.user.user.infrastructure.security.oauth.parse;

import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import com.example.meezy.bc.user.user.infrastructure.security.oauth.dto.OauthUserInfo;

import java.util.Map;

public interface OauthUserInfoParser {
    OauthUserInfo parse(Map<String, Object> attributes, OauthProvider provider);
}
