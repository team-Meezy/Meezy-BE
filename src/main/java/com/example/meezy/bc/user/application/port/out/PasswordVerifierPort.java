package com.example.meezy.bc.user.application.port.out;

public interface PasswordVerifierPort {

    boolean matches(String raw, String encoded);
    String encode(String raw);
}
