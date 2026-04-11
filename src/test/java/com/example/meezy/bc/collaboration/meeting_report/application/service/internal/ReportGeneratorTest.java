package com.example.meezy.bc.collaboration.meeting_report.application.service.internal;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.meeting_report.application.port.out.MeetingAnalyzerPort;
import com.example.meezy.bc.collaboration.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.collaboration.meeting_report.domain.MeetingReportStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportGenerator tests")
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
    private String title;

    @BeforeEach
    void setUp() {
        meetingId = UUID.randomUUID();
        transcript = "meeting transcript";
        summaryContent = "summary content";
        feedbackContent = "feedback content";
        title = "Sprint Review";
    }

    @Test
    @DisplayName("generate saves report with shared title")
    void generate_creates_and_saves_report() {
        given(meetingAnalyzerPort.generateSummary(anyString())).willReturn(summaryContent);
        given(meetingAnalyzerPort.generateFeedback(anyString())).willReturn(feedbackContent);
        given(meetingReportRepository.findByMeetingId(any())).willReturn(Optional.empty());

        reportGenerator.generate(meetingId, title, transcript);

        ArgumentCaptor<MeetingReport> captor = ArgumentCaptor.forClass(MeetingReport.class);
        verify(meetingReportRepository).save(captor.capture());

        MeetingReport savedReport = captor.getValue();
        assertThat(savedReport.getMeetingId().value()).isEqualTo(meetingId);
        assertThat(savedReport.getTitle()).isEqualTo(title);
        assertThat(savedReport.getSummary().getContent()).isEqualTo(summaryContent);
        assertThat(savedReport.getFeedback().getContent()).isEqualTo(feedbackContent);
    }

    @Test
    @DisplayName("generate delegates summary and feedback generation")
    void generate_calls_meeting_analyzer_port() {
        given(meetingAnalyzerPort.generateSummary(transcript)).willReturn(summaryContent);
        given(meetingAnalyzerPort.generateFeedback(transcript)).willReturn(feedbackContent);
        given(meetingReportRepository.findByMeetingId(any())).willReturn(Optional.empty());

        reportGenerator.generate(meetingId, title, transcript);

        verify(meetingAnalyzerPort).generateSummary(transcript);
        verify(meetingAnalyzerPort).generateFeedback(transcript);
    }

    @Test
    @DisplayName("markFailed stores source audio key and title")
    void markFailed_stores_source_audio_key() {
        given(meetingReportRepository.findByMeetingId(any())).willReturn(Optional.empty());

        reportGenerator.markFailed(meetingId, "recordings/test.mp3", title, "stt failed");

        ArgumentCaptor<MeetingReport> captor = ArgumentCaptor.forClass(MeetingReport.class);
        verify(meetingReportRepository).save(captor.capture());

        MeetingReport savedReport = captor.getValue();
        assertThat(savedReport.getStatus()).isEqualTo(MeetingReportStatus.FAILED);
        assertThat(savedReport.getFailureReason()).isEqualTo("stt failed");
        assertThat(savedReport.getSourceAudioKey()).isEqualTo("recordings/test.mp3");
        assertThat(savedReport.getTitle()).isEqualTo(title);
    }

    @Test
    @DisplayName("markProcessing stores source audio key and title")
    void markProcessing_stores_source_audio_key() {
        given(meetingReportRepository.findByMeetingId(any())).willReturn(Optional.empty());

        reportGenerator.markProcessing(meetingId, "recordings/test.mp3", title);

        ArgumentCaptor<MeetingReport> captor = ArgumentCaptor.forClass(MeetingReport.class);
        verify(meetingReportRepository).save(captor.capture());

        MeetingReport savedReport = captor.getValue();
        assertThat(savedReport.getMeetingId()).isEqualTo(MeetingId.of(meetingId));
        assertThat(savedReport.getStatus()).isEqualTo(MeetingReportStatus.PROCESSING);
        assertThat(savedReport.getSourceAudioKey()).isEqualTo("recordings/test.mp3");
        assertThat(savedReport.getTitle()).isEqualTo(title);
    }
}
