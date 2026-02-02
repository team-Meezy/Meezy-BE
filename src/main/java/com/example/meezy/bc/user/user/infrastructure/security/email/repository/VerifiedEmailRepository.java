package com.example.meezy.bc.user.user.infrastructure.security.email.repository;

import com.example.meezy.bc.user.user.infrastructure.security.email.VerifiedEmail;
import org.springframework.data.repository.CrudRepository;

public interface VerifiedEmailRepository extends CrudRepository<VerifiedEmail, String> {
}
