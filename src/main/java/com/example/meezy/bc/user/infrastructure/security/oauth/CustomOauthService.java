package com.example.meezy.bc.user.infrastructure.security.oauth;

import com.example.meezy.bc.user.application.service.internal.OauthUserService;
import com.example.meezy.bc.user.domain.type.OauthProvider;
import com.example.meezy.bc.user.infrastructure.security.auth.AuthDetails;
import com.example.meezy.bc.user.infrastructure.security.oauth.dto.OauthUserInfo;
import com.example.meezy.bc.user.infrastructure.security.oauth.parse.factory.OauthUserFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOauthService extends DefaultOAuth2UserService {

    private final OauthUserService oauthUserService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try{
            return process(userRequest, oAuth2User);
        } catch (AuthenticationException e){
            throw e;
        } catch (Exception e){
            throw new InternalAuthenticationServiceException(e.getMessage(), e.getCause());
        }
    }

    private OAuth2User process(OAuth2UserRequest userRequest, OAuth2User oAuth2User){
        OauthProvider provider = OauthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

        OauthUserInfo oauthUserInfo = OauthUserFactory.getParser(
                provider,
                oAuth2User.getAttributes()
        );

        return new AuthDetails(
                oauthUserService.resolveSocialUser(oauthUserInfo.email(), provider),
                oauthUserInfo.attributes()
        );
    }
}
