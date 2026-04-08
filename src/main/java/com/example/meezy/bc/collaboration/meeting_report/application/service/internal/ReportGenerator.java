package com.example.meezy.bc.collaboration.meeting_report.application.service.internal;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.collaboration.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.collaboration.meeting_report.domain.repository.MeetingReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerator {

    private final MeetingAnalyzerPort meetingAnalyzerPort;
    private final MeetingReportRepository meetingReportRepository;

    @Transactional
    public void generate(UUID meetingId, String transcript) {
        log.info("회의 리포트 조합 시작: meetingId={}, transcriptLength={}", meetingId, transcript.length());
        try {
            CompletableFuture<String> summaryFuture = CompletableFuture
                    .supplyAsync(() -> meetingAnalyzerPort.generateSummary(transcript));

            CompletableFuture<String> feedbackFuture = CompletableFuture
                    .supplyAsync(() -> meetingAnalyzerPort.generateFeedback(transcript));

            String summary = summaryFuture.join();
            String feedback = feedbackFuture.join();

            MeetingReport report = MeetingReport.create(
                    MeetingId.of(meetingId),
                    summary,
                    feedback
            );

            meetingReportRepository.save(report);
            log.info(
                    "회의 리포트 저장 완료: meetingId={}, summaryLength={}, feedbackLength={}",
                    meetingId,
                    summary.length(),
                    feedback.length()
            );
        } catch (Exception e) {
            log.error("회의 리포트 저장 실패: meetingId={}", meetingId, e);
            throw e;
        }
    }
}
