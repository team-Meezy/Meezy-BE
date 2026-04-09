package com.example.meezy.bc.collaboration.meeting_report.application.service.internal;

import com.example.meezy.bc.collaboration.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.collaboration.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.collaboration.meeting_report.domain.repository.MeetingReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportGenerator 테스트")
class ReportGeneratorTest {

    @Mock
    private MeetingAnalyzerPort meetingAnalyzerPort;

    @Mock
    private MeetingReportRepository meetingReportRepository;

    @InjectMocks
    private ReportGenerator reportGenerator;

    private UUID meetingId;
    private String transcript;
    private String summaryContent;
    private String feedbackContent;

    @BeforeEach
    void setUp() {
        meetingId = UUID.randomUUID();
        transcript = "회의 내용입니다.";
        summaryContent = "회의 요약입니다.";
        feedbackContent = "회의 피드백입니다.";
    }

    @Test
    @DisplayName("리포트를 생성하고 저장한다")
    void generate_creates_and_saves_report() {
        given(meetingAnalyzerPort.generateSummary(anyString())).willReturn(summaryContent);
        given(meetingAnalyzerPort.generateFeedback(anyString())).willReturn(feedbackContent);
        given(meetingReportRepository.findByMeetingId(org.mockito.ArgumentMatchers.any())).willReturn(Optional.empty());

        reportGenerator.generate(meetingId, transcript);

        ArgumentCaptor<MeetingReport> captor = ArgumentCaptor.forClass(MeetingReport.class);
        verify(meetingReportRepository).save(captor.capture());

        MeetingReport savedReport = captor.getValue();
        assertThat(savedReport.getMeetingId().value()).isEqualTo(meetingId);
        assertThat(savedReport.getSummary().getContent()).isEqualTo(summaryContent);
        assertThat(savedReport.getFeedback().getContent()).isEqualTo(feedbackContent);
    }

    @Test
    @DisplayName("요약과 피드백 생성을 위해 MeetingAnalyzerPort를 호출한다")
    void generate_calls_meeting_analyzer_port() {
        given(meetingAnalyzerPort.generateSummary(transcript)).willReturn(summaryContent);
        given(meetingAnalyzerPort.generateFeedback(transcript)).willReturn(feedbackContent);
        given(meetingReportRepository.findByMeetingId(org.mockito.ArgumentMatchers.any())).willReturn(Optional.empty());

        reportGenerator.generate(meetingId, transcript);

        verify(meetingAnalyzerPort).generateSummary(transcript);
        verify(meetingAnalyzerPort).generateFeedback(transcript);
    }
}
