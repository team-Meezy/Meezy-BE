package com.example.meezy.bc.sharedkernel.user;

import com.example.meezy.bc.user.user.domain.vo.UserId;
import lombok.Builder;

@Builder
public record AuthenticatedUser(
        UserId userId,
        String accountId,
        String name,
        String profileImageUrl
) {
}
