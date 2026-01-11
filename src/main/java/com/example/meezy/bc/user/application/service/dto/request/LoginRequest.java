package com.example.meezy.bc.user.application.service.dto.request;

public record LoginRequest(
        String accountId,
        String password
) {
}
