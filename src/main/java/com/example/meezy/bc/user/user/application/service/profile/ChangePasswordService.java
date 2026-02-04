package com.example.meezy.bc.user.user.application.service.profile;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.application.port.out.PasswordVerifierPort;
import com.example.meezy.bc.user.user.application.service.dto.request.ChangePasswordRequest;
import com.example.meezy.bc.user.user.application.service.exception.PasswordMisMatchException;
import com.example.meezy.bc.user.user.application.service.exception.UserNotFoundException;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordService {

    private final UserRepository userRepository;
    private final CurrentUserQuery currentUserQuery;
    private final PasswordVerifierPort passwordVerifierPort;

    @Transactional
    public void change(ChangePasswordRequest request){

        User user = userRepository.findByUserId(currentUserQuery.currentUser().userId())
                .orElseThrow(UserNotFoundException::new);

        if(!passwordVerifierPort.matches(request.currentPassword(), user.getPassword())){
            throw new PasswordMisMatchException();
        }

        user.changePassword(passwordVerifierPort.encode(request.newPassword()));
    }
}
