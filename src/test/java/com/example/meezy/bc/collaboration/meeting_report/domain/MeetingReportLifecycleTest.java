package com.example.meezy.bc.collaboration.meeting_report.domain;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MeetingReport lifecycle")
class MeetingReportLifecycleTest {

    @Test
    @DisplayName("processing report can complete with generated content and shared title")
    void processingReportCanComplete() {
        MeetingId meetingId = MeetingId.newId();
        MeetingReport report = MeetingReport.createProcessing(meetingId, "Sprint Review", "recordings/test.mp3");

        report.complete("Sprint Review", "summary", "feedback");

        assertThat(report.isCompleted()).isTrue();
        assertThat(report.getTitle()).isEqualTo("Sprint Review");
        assertThat(report.getSummary()).isNotNull();
        assertThat(report.getFeedback()).isNotNull();
        assertThat(report.getFailureReason()).isNull();
        assertThat(report.getSourceAudioKey()).isEqualTo("recordings/test.mp3");
    }

    @Test
    @DisplayName("processing report can fail and keep title metadata")
    void processingReportCanFail() {
        MeetingId meetingId = MeetingId.newId();
        MeetingReport report = MeetingReport.create(meetingId, "Sprint Review", "summary", "feedback");

        report.markProcessing("Sprint Review", "recordings/test.mp3");
        report.fail("stt failed");

        assertThat(report.isFailed()).isTrue();
        assertThat(report.getTitle()).isEqualTo("Sprint Review");
        assertThat(report.getSummary()).isNull();
        assertThat(report.getFeedback()).isNull();
        assertThat(report.getFailureReason()).isEqualTo("stt failed");
        assertThat(report.getSourceAudioKey()).isEqualTo("recordings/test.mp3");
    }
}
