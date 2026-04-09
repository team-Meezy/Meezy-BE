package com.example.meezy.bc.collaboration.meeting_report.domain;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.meeting_report.domain.vo.MeetingReportId;
import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_meeting_report")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MeetingReport extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "meeting_report_id"))
    private MeetingReportId meetingReportId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "meeting_id"))
    private MeetingId meetingId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Summary summary;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private Feedback feedback;

    @Enumerated(EnumType.STRING)
    @Column
    private MeetingReportStatus status;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(length = 1024)
    private String sourceAudioKey;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static MeetingReport create(MeetingId meetingId, String summaryContent, String feedbackContent) {
        return MeetingReport.builder()
                .meetingReportId(MeetingReportId.newId())
                .meetingId(meetingId)
                .summary(Summary.create(meetingId, summaryContent))
                .feedback(Feedback.create(meetingId, feedbackContent))
                .status(MeetingReportStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static MeetingReport createProcessing(MeetingId meetingId) {
        return createProcessing(meetingId, null);
    }

    public static MeetingReport createProcessing(MeetingId meetingId, String sourceAudioKey) {
        return MeetingReport.builder()
                .meetingReportId(MeetingReportId.newId())
                .meetingId(meetingId)
                .status(MeetingReportStatus.PROCESSING)
                .sourceAudioKey(normalizeSourceAudioKey(sourceAudioKey))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void markProcessing(String sourceAudioKey) {
        this.summary = null;
        this.feedback = null;
        this.status = MeetingReportStatus.PROCESSING;
        this.failureReason = null;
        this.sourceAudioKey = normalizeSourceAudioKey(sourceAudioKey);
    }

    public void complete(String summaryContent, String feedbackContent) {
        this.summary = Summary.create(meetingId, summaryContent);
        this.feedback = Feedback.create(meetingId, feedbackContent);
        this.status = MeetingReportStatus.COMPLETED;
        this.failureReason = null;
    }

    public void fail(String reason) {
        this.summary = null;
        this.feedback = null;
        this.status = MeetingReportStatus.FAILED;
        this.failureReason = truncate(reason);
    }

    public boolean isCompleted() {
        return this.status == null || this.status == MeetingReportStatus.COMPLETED;
    }

    public boolean isFailed() {
        return this.status == MeetingReportStatus.FAILED;
    }

    public void updateSummaryContent(String content) {
        this.summary.updateContent(content);
    }

    public void updateFeedbackContent(String content) {
        this.feedback.updateContent(content);
    }

    public void updateSourceAudioKey(String sourceAudioKey) {
        this.sourceAudioKey = normalizeSourceAudioKey(sourceAudioKey);
    }

    private String truncate(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.length() <= 1000 ? reason : reason.substring(0, 1000);
    }

    private static String normalizeSourceAudioKey(String sourceAudioKey) {
        if (sourceAudioKey == null || sourceAudioKey.isBlank()) {
            return null;
        }
        return sourceAudioKey.length() <= 1024 ? sourceAudioKey : sourceAudioKey.substring(0, 1024);
    }
}
