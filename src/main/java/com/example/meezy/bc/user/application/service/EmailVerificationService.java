package com.example.meezy.bc.user.application.service;

import com.example.meezy.bc.user.application.port.out.EmailSenderPort;
import com.example.meezy.bc.user.application.port.out.EmailVerificationPort;
import com.example.meezy.bc.user.application.service.dto.request.SendVerificationRequest;
import com.example.meezy.bc.user.application.service.dto.request.VerifyEmailRequest;
import com.example.meezy.bc.user.application.service.dto.response.SendVerificationResponse;
import com.example.meezy.bc.user.application.service.dto.response.VerifyEmailResponse;
import com.example.meezy.bc.user.application.service.exception.DuplicateEmailException;
import com.example.meezy.bc.user.application.service.exception.InvalidVerificationCodeException;
import com.example.meezy.bc.user.application.service.exception.VerificationAttemptExceededException;
import com.example.meezy.bc.user.domain.repository.UserRepository;
import com.example.meezy.bc.user.domain.type.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final long VERIFICATION_CODE_TTL_SECONDS = 5 * 60; // 5분
    private static final int CODE_LENGTH = 6;

    private final EmailVerificationPort emailVerificationPort;
    private final EmailSenderPort emailSenderPort;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public SendVerificationResponse sendVerificationCode(SendVerificationRequest request) {
        String email = request.email();

        if (userRepository.existsByEmailAndOauthProvider(email, OauthProvider.LOCAL)) {
            throw new DuplicateEmailException();
        }

        if (emailVerificationPort.isBlocked(email)) {
            throw new VerificationAttemptExceededException();
        }

        String code = generateVerificationCode();
        int remainingAttempts = emailVerificationPort.getRemainingAttempts(email); // 남은 횟수 먼저 조회

        emailVerificationPort.incrementAttempt(email); // 시도 횟수 증가
        emailVerificationPort.saveVerificationCode(email, code, VERIFICATION_CODE_TTL_SECONDS); // 5분 인증 코드 저장
        emailSenderPort.sendVerificationEmail(email, code); // 메일 발송

        return SendVerificationResponse.builder()
                .email(email)
                .remainingAttempts(remainingAttempts)
                .message("인증 코드가 발송되었습니다.")
                .build();
    }

    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {
        String email = request.email();
        String code = request.code();

        if (!emailVerificationPort.verifyCode(email, code)) { //만료 되었거나 다르다면 false
            throw new InvalidVerificationCodeException();
        }

        emailVerificationPort.deleteVerificationCode(email); //성공 시 코드 파기
        emailVerificationPort.markAsVerified(email); //이메일 인증 성공 상태 30분 유지

        return VerifyEmailResponse.builder()
                .email(email)
                .verified(true)
                .message("이메일 인증이 완료되었습니다.")
                .build();
    }

    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }
        return code.toString();
    }
}
