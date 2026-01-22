package com.example.meezy.bc.collaboration.meeting_report.domain;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MeetingReport 도메인 테스트")
class MeetingReportTest {

    private final MeetingId meetingId = MeetingId.newId();

    @Nested
    @DisplayName("리포트 생성")
    class CreateTest {

        @Test
        @DisplayName("리포트를 생성하면 요약과 피드백이 함께 생성된다")
        void create_generates_summary_and_feedback() {
            String summaryContent = "회의 요약 내용입니다.";
            String feedbackContent = "회의 피드백 내용입니다.";

            MeetingReport report = MeetingReport.create(meetingId, summaryContent, feedbackContent);

            assertThat(report.getMeetingReportId()).isNotNull();
            assertThat(report.getMeetingId()).isEqualTo(meetingId);
            assertThat(report.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("생성된 요약의 내용이 올바르게 설정된다")
        void create_sets_summary_content() {
            String summaryContent = "회의 요약 내용입니다.";

            MeetingReport report = MeetingReport.create(meetingId, summaryContent, "피드백");

            assertThat(report.getSummary()).isNotNull();
            assertThat(report.getSummary().getContent()).isEqualTo(summaryContent);
            assertThat(report.getSummary().getMeetingId()).isEqualTo(meetingId);
            assertThat(report.getSummary().getSummaryId()).isNotNull();
        }

        @Test
        @DisplayName("생성된 피드백의 내용이 올바르게 설정된다")
        void create_sets_feedback_content() {
            String feedbackContent = "회의 피드백 내용입니다.";

            MeetingReport report = MeetingReport.create(meetingId, "요약", feedbackContent);

            assertThat(report.getFeedback()).isNotNull();
            assertThat(report.getFeedback().getContent()).isEqualTo(feedbackContent);
            assertThat(report.getFeedback().getMeetingId()).isEqualTo(meetingId);
            assertThat(report.getFeedback().getFeedbackId()).isNotNull();
        }
    }

    @Nested
    @DisplayName("요약 수정")
    class UpdateSummaryTest {

        @Test
        @DisplayName("요약 내용을 수정할 수 있다")
        void updateSummaryContent_updates_content() {
            MeetingReport report = MeetingReport.create(meetingId, "기존 요약", "피드백");
            String newContent = "수정된 요약 내용입니다.";

            report.updateSummaryContent(newContent);

            assertThat(report.getSummary().getContent()).isEqualTo(newContent);
        }
    }

    @Nested
    @DisplayName("피드백 수정")
    class UpdateFeedbackTest {

        @Test
        @DisplayName("피드백 내용을 수정할 수 있다")
        void updateFeedbackContent_updates_content() {
            MeetingReport report = MeetingReport.create(meetingId, "요약", "기존 피드백");
            String newContent = "수정된 피드백 내용입니다.";

            report.updateFeedbackContent(newContent);

            assertThat(report.getFeedback().getContent()).isEqualTo(newContent);
        }
    }
}
