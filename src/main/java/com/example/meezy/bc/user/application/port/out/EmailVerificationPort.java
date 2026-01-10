package com.example.meezy.bc.user.application.port.out;

public interface EmailVerificationPort {

    void saveVerificationCode(String email, String code, long ttlSeconds);

    boolean verifyCode(String email, String code);

    void deleteVerificationCode(String email);

    int getAttemptCount(String email);

    void incrementAttempt(String email);

    boolean isBlocked(String email);

    int getRemainingAttempts(String email);

    void markAsVerified(String email);

    boolean isVerified(String email);

    void deleteVerifiedEmail(String email);
}
