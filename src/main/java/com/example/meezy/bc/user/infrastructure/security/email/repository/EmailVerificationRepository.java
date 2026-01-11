package com.example.meezy.bc.user.infrastructure.security.email.repository;

import com.example.meezy.bc.user.infrastructure.security.email.EmailVerification;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmailVerificationRepository extends CrudRepository<EmailVerification, String> {
    Optional<EmailVerification> findByCode(String code);
}
