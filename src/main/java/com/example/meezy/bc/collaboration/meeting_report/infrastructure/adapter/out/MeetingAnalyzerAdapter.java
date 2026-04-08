package com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out;

import com.example.meezy.bc.collaboration.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.FeedbackGenerator;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.Mp3ChunkSplitter;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.SpeechToTextClient;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.SummaryGenerator;
import com.example.meezy.bc.sharedkernel.file.AudioStoragePort;
import lombok.RequiredArgsConstructor;
3import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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
        log.info(
                "회의 음성 변환 시작: s3Key={}, audioBytes={}, chunkCount={}",
                s3Key,
                audioData.length,
                chunks.size()
        );

        List<String> transcripts = new ArrayList<>(chunks.size());

        for (int i = 0; i < chunks.size(); i++) {
            byte[] chunk = chunks.get(i);
            try {
                transcripts.add(speechToTextClient.transcribe(chunk));
            } catch (Exception e) {
                log.error(
                        "회의 음성 변환 실패: s3Key={}, chunkIndex={}, chunkCount={}, chunkBytes={}",
                        s3Key,
                        i + 1,
                        chunks.size(),
                        chunk.length,
                        e
                );
                throw e;
            }
        }

        String transcript = String.join(" ", transcripts);
        log.info("회의 음성 변환 완료: s3Key={}, transcriptLength={}", s3Key, transcript.length());
        return transcript;
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
