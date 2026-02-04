package com.example.meezy.bc.user.user.application.service.dto.response;

import com.example.meezy.bc.user.user.domain.User;
import lombok.Builder;

@Builder
public record MyProfileResponse(
        String profileImage,
        String name,
        String accountId,
        String email
) {

    public static MyProfileResponse from(User user){
        return MyProfileResponse.builder()
                .profileImage(user.getProfileImageUrl())
                .name(user.getName())
                .accountId(user.getAccountId())
                .email(user.getEmail())
                .build();
    }
}
