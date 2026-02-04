package com.example.meezy.bc.user.user.application.service.profile;

import com.example.meezy.bc.sharedkernel.file.FileStoragePort;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.application.port.out.PasswordVerifierPort;
import com.example.meezy.bc.user.user.application.service.dto.request.WithdrawRequest;
import com.example.meezy.bc.user.user.application.service.exception.PasswordMisMatchException;
import com.example.meezy.bc.user.user.application.service.exception.UserNotFoundException;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawService {

    private final UserRepository userRepository;
    private final CurrentUserQuery currentUserQuery;
    private final PasswordVerifierPort passwordVerifierPort;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public void withdraw(WithdrawRequest request) {

        User user = userRepository.findByUserId(currentUserQuery.currentUser().userId())
                .orElseThrow(UserNotFoundException::new);

        if (!passwordVerifierPort.matches(request.password(), user.getPassword())) {
            throw new PasswordMisMatchException();
        }

        deleteProfileImageIfExists(user);

        userRepository.delete(user);
    }

    private void deleteProfileImageIfExists(User user) {
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
            fileStoragePort.deleteByKey(user.getProfileImageUrl());
        }
    }
}
