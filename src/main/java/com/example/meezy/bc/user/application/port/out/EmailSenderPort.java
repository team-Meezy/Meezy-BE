package com.example.meezy.bc.user.application.port.out;

public interface EmailSenderPort {

    void sendVerificationEmail(String to, String code);
}
