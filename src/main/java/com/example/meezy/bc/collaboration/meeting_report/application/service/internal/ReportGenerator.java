package com.example.meezy.bc.collaboration.meeting_report.application.service.internal;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.collaboration.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.collaboration.meeting_report.domain.repository.MeetingReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ReportGenerator {

    private final MeetingAnalyzerPort meetingAnalyzerPort;
    private final MeetingReportRepository meetingReportRepository;

    @Transactional
    public void generate(UUID meetingId, String transcript) {
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
    }
}
