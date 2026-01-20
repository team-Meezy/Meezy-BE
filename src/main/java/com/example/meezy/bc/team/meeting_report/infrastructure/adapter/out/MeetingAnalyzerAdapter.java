package com.example.meezy.bc.team.meeting_report.infrastructure.adapter.out;

import com.example.meezy.bc.team.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.team.meeting_report.infrastructure.ai.FeedbackGenerator;
import com.example.meezy.bc.team.meeting_report.infrastructure.ai.SpeechToTextClient;
import com.example.meezy.bc.team.meeting_report.infrastructure.ai.SummaryGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingAnalyzerAdapter implements MeetingAnalyzerPort {

    private final SpeechToTextClient speechToTextClient;
    private final SummaryGenerator summaryGenerator;
    private final FeedbackGenerator feedbackGenerator;

    @Override
    public String transcribe(byte[] audioData, String originalFilename, String contentType) {
        return speechToTextClient.transcribe(audioData, originalFilename, contentType);
    }

    @Override
    public String generateSummary(String transcript) {
        return summaryGenerator.summary(transcript);
    }

    @Override
    public String generateFeedback(String transcript) {
        return feedbackGenerator.feedback(transcript);
    }
}
