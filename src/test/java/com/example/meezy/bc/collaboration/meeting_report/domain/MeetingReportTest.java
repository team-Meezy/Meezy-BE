package com.example.meezy.bc.collaboration.meeting_report.domain;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MeetingReport domain tests")
class MeetingReportTest {

    private final MeetingId meetingId = MeetingId.newId();

    @Nested
    @DisplayName("create")
    class CreateTest {

        @Test
        @DisplayName("creates summary, feedback, and shared title")
        void create_generates_summary_and_feedback() {
            MeetingReport report = MeetingReport.create(meetingId, "Sprint Review", "summary", "feedback");

            assertThat(report.getMeetingReportId()).isNotNull();
            assertThat(report.getMeetingId()).isEqualTo(meetingId);
            assertThat(report.getTitle()).isEqualTo("Sprint Review");
            assertThat(report.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("sets summary content")
        void create_sets_summary_content() {
            MeetingReport report = MeetingReport.create(meetingId, "Sprint Review", "summary", "feedback");

            assertThat(report.getSummary()).isNotNull();
            assertThat(report.getSummary().getContent()).isEqualTo("summary");
            assertThat(report.getSummary().getMeetingId()).isEqualTo(meetingId);
            assertThat(report.getSummary().getSummaryId()).isNotNull();
        }

        @Test
        @DisplayName("sets feedback content")
        void create_sets_feedback_content() {
            MeetingReport report = MeetingReport.create(meetingId, "Sprint Review", "summary", "feedback");

            assertThat(report.getFeedback()).isNotNull();
            assertThat(report.getFeedback().getContent()).isEqualTo("feedback");
            assertThat(report.getFeedback().getMeetingId()).isEqualTo(meetingId);
            assertThat(report.getFeedback().getFeedbackId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("updates summary content")
        void updateSummaryContent_updates_content() {
            MeetingReport report = MeetingReport.create(meetingId, "Sprint Review", "old summary", "feedback");

            report.updateSummaryContent("new summary");

            assertThat(report.getSummary().getContent()).isEqualTo("new summary");
        }

        @Test
        @DisplayName("updates feedback content")
        void updateFeedbackContent_updates_content() {
            MeetingReport report = MeetingReport.create(meetingId, "Sprint Review", "summary", "old feedback");

            report.updateFeedbackContent("new feedback");

            assertThat(report.getFeedback().getContent()).isEqualTo("new feedback");
        }
    }
}
