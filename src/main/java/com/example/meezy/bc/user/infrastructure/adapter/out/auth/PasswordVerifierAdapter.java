package com.example.meezy.bc.user.infrastructure.adapter.out.auth;

import com.example.meezy.bc.user.application.port.out.PasswordVerifierPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordVerifierAdapter implements PasswordVerifierPort {

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean matches(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }

    @Override
    public String encode(String raw) {
        return passwordEncoder.encode(raw);
    }
}
