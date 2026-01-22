package com.example.meezy.bc.collaboration.meeting_report.domain;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.meeting_report.domain.vo.FeedbackId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_feedback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Feedback {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "feedback_id"))
    private FeedbackId feedbackId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "meeting_id"))
    private MeetingId meetingId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public static Feedback create(MeetingId meetingId, String content) {
        return Feedback.builder()
                .feedbackId(FeedbackId.newId())
                .meetingId(meetingId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateContent(String content){
        this.content = content;
    }
}
