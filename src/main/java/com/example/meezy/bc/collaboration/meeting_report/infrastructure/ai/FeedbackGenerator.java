package com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedbackGenerator {

    private final ChatClient client;

    @Value("${spring.ai.openai.prompt.feedback}")
    private String systemPrompt;

    private static final String FEEDBACK_USER_PROMPT = "해당 글은 회의 내용에 대한 TEXT야. 알맞게 회의 피드백을 수행해줘.\n";

    public String feedback(String meetingContent){
        log.info("AI 피드백 생성 시작: transcriptLength={}", meetingContent.length());
        try {
            String feedback = client.prompt()
                    .system(systemPrompt)
                    .user(FEEDBACK_USER_PROMPT + meetingContent)
                    .call()
                    .content();
            log.info("AI 피드백 생성 완료: feedbackLength={}", feedback != null ? feedback.length() : 0);
            return feedback;
        } catch (Exception e) {
            log.error("AI 피드백 생성 실패: transcriptLength={}", meetingContent.length(), e);
            throw e;
        }
    }
}
