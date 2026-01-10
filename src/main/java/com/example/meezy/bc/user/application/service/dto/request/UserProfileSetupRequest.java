package com.example.meezy.bc.user.application.service.dto.request;

public record UserProfileSetupRequest(
        String accountId,
        String name,
        String password
) {
}
