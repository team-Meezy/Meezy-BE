package com.example.meezy.bc.user.user.infrastructure.adapter.out.email;

import com.example.meezy.bc.user.user.application.port.out.EmailVerificationPort;
import com.example.meezy.bc.user.user.infrastructure.security.email.EmailVerification;
import com.example.meezy.bc.user.user.infrastructure.security.email.EmailVerificationAttempt;
import com.example.meezy.bc.user.user.infrastructure.security.email.VerifiedEmail;
import com.example.meezy.bc.user.user.infrastructure.security.email.repository.EmailVerificationAttemptRepository;
import com.example.meezy.bc.user.user.infrastructure.security.email.repository.EmailVerificationRepository;
import com.example.meezy.bc.user.user.infrastructure.security.email.repository.VerifiedEmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailVerificationAdapter implements EmailVerificationPort {

    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailVerificationAttemptRepository attemptRepository;
    private final VerifiedEmailRepository verifiedEmailRepository;

    @Override
    public void saveVerificationCode(String email, String code, long ttlSeconds) {
        emailVerificationRepository.save(
                EmailVerification.builder()
                        .email(email)
                        .code(code)
                        .ttl(ttlSeconds)
                        .build()
        );
    }

    @Override
    public boolean verifyCode(String email, String code) {
        return emailVerificationRepository.findById(email)
                .map(verification -> verification.getCode().equals(code))
                .orElse(false);
    }

    @Override
    public void deleteVerificationCode(String email) {
        emailVerificationRepository.deleteById(email);
    }

    @Override
    public int getAttemptCount(String email) {
        return attemptRepository.findById(email)
                .map(EmailVerificationAttempt::getAttemptCount)
                .orElse(0);
    }

    @Override
    public void incrementAttempt(String email) {
        EmailVerificationAttempt attempt = attemptRepository.findById(email)
                .map(EmailVerificationAttempt::incrementAttempt)
                .orElseGet(() -> EmailVerificationAttempt.createNew(email));

        attemptRepository.save(attempt);
    }

    @Override
    public boolean isBlocked(String email) {
        return attemptRepository.findById(email)
                .map(EmailVerificationAttempt::isBlocked)
                .orElse(false);
    }

    @Override
    public int getRemainingAttempts(String email) {
        return attemptRepository.findById(email)
                .map(EmailVerificationAttempt::getRemainingAttempts)
                .orElse(5);
    }

    @Override
    public void markAsVerified(String email) {
        verifiedEmailRepository.save(VerifiedEmail.of(email));
    }

    @Override
    public boolean isVerified(String email) {
        return verifiedEmailRepository.existsById(email);
    }

    @Override
    public void deleteVerifiedEmail(String email) {
        verifiedEmailRepository.deleteById(email);
    }
}
