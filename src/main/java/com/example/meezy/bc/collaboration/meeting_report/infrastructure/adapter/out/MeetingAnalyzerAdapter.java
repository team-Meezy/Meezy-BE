package com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out;

import com.example.meezy.bc.collaboration.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.FeedbackGenerator;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.Mp3ChunkSplitter;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.SpeechToTextClient;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.SummaryGenerator;
import com.example.meezy.bc.sharedkernel.file.AudioStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MeetingAnalyzerAdapter implements MeetingAnalyzerPort {

    private final SpeechToTextClient speechToTextClient;
    private final SummaryGenerator summaryGenerator;
    private final FeedbackGenerator feedbackGenerator;
    private final AudioStoragePort audioStoragePort;
    private final Mp3ChunkSplitter mp3ChunkSplitter;

    @Override
    public String transcribe(String s3Key) {
        byte[] audioData = audioStoragePort.downloadAudio(s3Key);
        List<byte[]> chunks = mp3ChunkSplitter.split(audioData);

        return chunks.stream()
                .map(speechToTextClient::transcribe)
                .collect(Collectors.joining(" "));
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
