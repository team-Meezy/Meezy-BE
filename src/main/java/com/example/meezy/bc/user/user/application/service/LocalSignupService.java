package com.example.meezy.bc.user.user.application.service;

import com.example.meezy.bc.user.user.application.port.out.EmailVerificationPort;
import com.example.meezy.bc.user.user.application.port.out.PasswordVerifierPort;
import com.example.meezy.bc.user.user.application.service.dto.request.SignupRequest;
import com.example.meezy.bc.user.user.application.service.exception.DuplicateAccountIdException;
import com.example.meezy.bc.user.user.application.service.exception.DuplicateEmailException;
import com.example.meezy.bc.user.user.application.service.exception.EmailNotVerifiedException;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import com.example.meezy.bc.user.user.domain.type.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocalSignupService {

    private final UserRepository userRepository;
    private final PasswordVerifierPort passwordVerifierPort;
    private final EmailVerificationPort emailVerificationPort;

    @Transactional
    public void signup(SignupRequest request){
        if (!emailVerificationPort.isVerified(request.email())) { //인증이 성공한 이메일인지 확인
            throw new EmailNotVerifiedException();
        }

        if (userRepository.existsByAccountId(request.accountId())) {
            throw new DuplicateAccountIdException();
        }

        if (userRepository.existsByEmailAndOauthProvider(request.email(), OauthProvider.LOCAL)) {
            throw new DuplicateEmailException();
        }

        userRepository.save(
                User.createLocal(
                        request.email(),
                        request.accountId(),
                        request.name(),
                        passwordVerifierPort.encode(request.password())
                )
        );

        emailVerificationPort.deleteVerifiedEmail(request.email());
    }
}
