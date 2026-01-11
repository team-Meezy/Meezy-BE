package com.example.meezy.bc.user.infrastructure.adapter.out.auth;

import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.domain.User;
import com.example.meezy.bc.user.infrastructure.security.auth.AuthDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserAdapter implements CurrentUserQuery {


    @Override
    public AuthenticatedUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        User detachedUser = authDetails.getUser();

        return AuthenticatedUser.builder()
                .userId(detachedUser.getUserId())
                .accountId(detachedUser.getAccountId())
                .build();
    }
}
