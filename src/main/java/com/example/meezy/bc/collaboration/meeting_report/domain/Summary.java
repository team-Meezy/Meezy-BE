package com.example.meezy.bc.collaboration.meeting_report.domain;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.meeting_report.domain.vo.SummaryId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_summary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Summary {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "summary_id"))
    private SummaryId summaryId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "meeting_id"))
    private MeetingId meetingId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;


    public static Summary create(MeetingId meetingId, String content) {
        return Summary.builder()
                .summaryId(SummaryId.newId())
                .meetingId(meetingId)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void updateContent(String content){
        this.content = content;
    }
}
