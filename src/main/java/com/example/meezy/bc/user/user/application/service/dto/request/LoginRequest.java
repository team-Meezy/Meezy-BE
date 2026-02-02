package com.example.meezy.bc.user.user.application.service.dto.request;

public record LoginRequest(
        String accountId,
        String password
) {
}
