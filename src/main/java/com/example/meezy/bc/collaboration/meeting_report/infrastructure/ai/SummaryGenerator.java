package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SummaryGenerator {

    private final ChatClient client;

    @Value("${spring.ai.openai.prompt.summary}")
    private String systemPrompt;


    private static final String SUMMARY_USER_PROMPT = "해당 글은 회의 내용에 대한 TEXT야. 알맞게 회의 요약을 수행해줘.\n";


    public String summary(String meetingContent){
        log.info("AI 요약 생성 시작: transcriptLength={}", meetingContent.length());
        try {
            String summary = client.prompt()
                    .system(systemPrompt)
                    .user(SUMMARY_USER_PROMPT + meetingContent)
                    .call()
                    .content();
            log.info("AI 요약 생성 완료: summaryLength={}", summary != null ? summary.length() : 0);
            return summary;
        } catch (Exception e) {
            log.error("AI 요약 생성 실패: transcriptLength={}", meetingContent.length(), e);
            throw e;
        }
    }
}
