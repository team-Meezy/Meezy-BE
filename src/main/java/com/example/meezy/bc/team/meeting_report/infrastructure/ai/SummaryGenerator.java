package com.example.meezy.bc.team.meeting_report.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SummaryGenerator {

    private ChatClient client;

    @Value("${spring.ai.openai.prompt.summary}")
    private String systemPrompt;


    private static final String SUMMARY_USER_PROMPT = "해당 글은 회의 내용에 대한 TEXT야. 알맞게 회의 요약을 수행해줘.\n";


    public String summary(String meetingContent){
        return client.prompt()
                .system(systemPrompt)
                .user(SUMMARY_USER_PROMPT + meetingContent)
                .call()
                .content();
    }
}
