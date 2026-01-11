package com.example.meezy.bc.user.presentation;

import com.example.meezy.bc.user.application.service.EmailVerificationService;
import com.example.meezy.bc.user.application.service.dto.request.SendVerificationRequest;
import com.example.meezy.bc.user.application.service.dto.request.VerifyEmailRequest;
import com.example.meezy.bc.user.application.service.dto.response.SendVerificationResponse;
import com.example.meezy.bc.user.application.service.dto.response.VerifyEmailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public SendVerificationResponse sendVerificationCode(
            @Valid @RequestBody SendVerificationRequest request
    ) {
        return emailVerificationService.sendVerificationCode(request);
    }

    @PostMapping("/verify")
    public VerifyEmailResponse verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request
    ) {
        return emailVerificationService.verifyEmail(request);
    }
}
