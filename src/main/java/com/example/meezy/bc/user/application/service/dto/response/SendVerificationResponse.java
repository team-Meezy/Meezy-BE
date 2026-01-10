package com.example.meezy.bc.user.application.service.dto.response;

import lombok.Builder;

@Builder
public record SendVerificationResponse(
        String email,
        int remainingAttempts,
        String message
) {}
