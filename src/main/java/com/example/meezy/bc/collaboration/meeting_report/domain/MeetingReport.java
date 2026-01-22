package com.example.meezy.bc.collaboration.meeting_report.domain;

import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.meeting_report.domain.vo.MeetingReportId;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static MeetingReport create(MeetingId meetingId, String summaryContent, String feedbackContent) {
        return MeetingReport.builder()
                .meetingReportId(MeetingReportId.newId())
                .meetingId(meetingId)
                .summary(Summary.create(meetingId, summaryContent))
                .feedback(Feedback.create(meetingId, feedbackContent))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateSummaryContent(String content){
        this.summary.updateContent(content);
    }

    public void updateFeedbackContent(String content){
        this.feedback.updateContent(content);
    }
}
