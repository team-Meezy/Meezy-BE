package com.example.meezy.bc.user.user.application.service.dto.response;

import lombok.Builder;

@Builder
public record VerifyEmailResponse(
        String email,
        boolean verified,
        String message
) {}
