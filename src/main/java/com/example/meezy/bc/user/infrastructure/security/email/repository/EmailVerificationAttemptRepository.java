package com.example.meezy.bc.user.infrastructure.security.email.repository;

import com.example.meezy.bc.user.infrastructure.security.email.EmailVerificationAttempt;
import org.springframework.data.repository.CrudRepository;

public interface EmailVerificationAttemptRepository extends CrudRepository<EmailVerificationAttempt, String> {
}
