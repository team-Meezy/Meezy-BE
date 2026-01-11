package com.example.meezy.bc.user.infrastructure.adapter.out.email;

import com.example.meezy.bc.user.application.port.out.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSenderAdapter implements EmailSenderPort {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[Meezy] 이메일 인증 코드");
        message.setText(buildVerificationEmailBody(code));

        mailSender.send(message);
    }

    private String buildVerificationEmailBody(String code) {
        return String.format("""
                안녕하세요, Meezy입니다.

                아래 인증 코드를 입력해주세요.

                인증 코드: %s

                이 코드는 5분간 유효합니다.

                본인이 요청하지 않은 경우 이 메일을 무시해주세요.
                """, code);
    }
}
