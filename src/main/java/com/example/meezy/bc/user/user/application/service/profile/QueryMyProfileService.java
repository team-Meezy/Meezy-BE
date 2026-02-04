package com.example.meezy.bc.user.user.application.service.profile;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.application.service.dto.response.MyProfileResponse;
import com.example.meezy.bc.user.user.application.service.exception.UserNotFoundException;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QueryMyProfileService {

    private final CurrentUserQuery currentUserQuery;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public MyProfileResponse query(){
        return MyProfileResponse.from(
                userRepository.findByUserId(currentUserQuery.currentUser().userId())
                        .orElseThrow(UserNotFoundException::new)
        );
    }
}
