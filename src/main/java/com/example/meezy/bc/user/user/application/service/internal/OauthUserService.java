package com.example.meezy.bc.user.user.application.service.internal;

import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OauthUserService {

    private final UserRepository userRepository;

    @Transactional
    public User resolveSocialUser(String email, OauthProvider provider){
        return userRepository.findByEmailAndOauthProvider(email, provider)
                .orElseGet(() -> userRepository.save(
                        User.createOauth(email, provider))
                );
    }
}
