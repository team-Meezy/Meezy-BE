package com.example.meezy.bc.user.user.application.service;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.application.port.out.PasswordVerifierPort;
import com.example.meezy.bc.user.user.application.service.dto.request.UserProfileSetupRequest;
import com.example.meezy.bc.user.user.application.service.dto.response.ProfileSetupResponse;
import com.example.meezy.bc.user.user.application.service.exception.DuplicateAccountIdException;
import com.example.meezy.bc.user.user.application.service.exception.ProfileAlreadyCompletedException;
import com.example.meezy.bc.user.user.application.service.exception.UserNotFoundException;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileSetupService {

    private final UserRepository userRepository;
    private final PasswordVerifierPort passwordVerifierPort;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public ProfileSetupResponse setup(UserProfileSetupRequest request){
        UserId userId = currentUserQuery.currentUser().userId();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(UserNotFoundException::new);

        if(user.isProfileCompleted()){
            throw new ProfileAlreadyCompletedException();
        }

        if(userRepository.existsByAccountId(request.accountId())){
            throw new DuplicateAccountIdException();
        }

        user.completeProfile(request.accountId(), request.name(), passwordVerifierPort.encode(request.password()));

        return ProfileSetupResponse.builder()
                .isProfileCompleted(true)
                .build();
    }
}
