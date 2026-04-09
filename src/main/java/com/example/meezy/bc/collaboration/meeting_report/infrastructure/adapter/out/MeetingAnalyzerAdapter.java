package com.example.meezy.bc.collaboration.meeting_report.infrastructure.adapter.out;

import com.example.meezy.bc.collaboration.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.FeedbackGenerator;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.FfmpegAudioChunker;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.PreparedAudio;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.SpeechToTextClient;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.SummaryGenerator;
import com.example.meezy.bc.collaboration.meeting_report.infrastructure.ai.exception.AudioConversionException;
import com.example.meezy.bc.sharedkernel.file.AudioStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private final FfmpegAudioChunker audioChunker;

    @Override
    public String transcribe(String s3Key) {
        Path downloadedAudio = audioStoragePort.downloadAudioToTempFile(s3Key);
        try (PreparedAudio preparedAudio = audioChunker.prepare(downloadedAudio)) {
            long audioBytes = Files.size(downloadedAudio);
            List<Path> chunks = preparedAudio.chunkPaths();
            log.info(
                    "회의 음성 변환 시작: s3Key={}, audioBytes={}, chunkCount={}",
                    s3Key,
                    audioBytes,
                    chunks.size()
            );

            List<String> transcripts = new ArrayList<>(chunks.size());

            for (int i = 0; i < chunks.size(); i++) {
                Path chunk = chunks.get(i);
                try {
                    transcripts.add(speechToTextClient.transcribe(chunk));
                } catch (Exception e) {
                    log.error(
                            "회의 음성 변환 실패: s3Key={}, chunkIndex={}, chunkCount={}, chunkBytes={}",
                            s3Key,
                            i + 1,
                            chunks.size(),
                            safeSize(chunk),
                            e
                    );
                    throw e;
                }
            }

            String transcript = String.join(" ", transcripts);
            log.info("회의 음성 변환 완료: s3Key={}, transcriptLength={}", s3Key, transcript.length());
            return transcript;
        } catch (IOException e) {
            log.error("회의 음성 파일 크기 조회 실패: s3Key={}", s3Key, e);
            throw new AudioConversionException();
        } finally {
            deleteTempFile(downloadedAudio);
        }
    }

    @Override
    public String generateSummary(String transcript) {
        return summaryGenerator.summary(transcript);
    }

    @Override
    public String generateFeedback(String transcript) {
        return feedbackGenerator.feedback(transcript);
    }

    private void deleteTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            log.warn("다운로드 임시 오디오 파일 삭제 실패: {}", tempFile, e);
        }
    }

    private long safeSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            log.warn("오디오 청크 크기 조회 실패: {}", file, e);
            return -1L;
        }
    }
}
