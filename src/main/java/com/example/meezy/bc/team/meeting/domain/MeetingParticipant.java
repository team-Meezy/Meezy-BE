package com.example.meezy.bc.team.meeting.domain;

import com.example.meezy.bc.team.meeting.domain.vo.MeetingParticipantId;
import com.example.meezy.bc.user.domain.vo.UserId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_meeting_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MeetingParticipant {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "meeting_participant_id"))
    private MeetingParticipantId meetingParticipantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Column
    private LocalDateTime leftAt;

    @Column(nullable = false)
    private boolean isActive;

    public static MeetingParticipant create(UserId userId, Meeting meeting) {
        return MeetingParticipant.builder()
                .meetingParticipantId(MeetingParticipantId.newId())
                .meeting(meeting)
                .userId(userId)
                .joinedAt(LocalDateTime.now())
                .leftAt(null)
                .isActive(true)
                .build();
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
        this.isActive = false;
    }

    public void rejoin() {
        this.joinedAt = LocalDateTime.now();
        this.leftAt = null;
        this.isActive = true;
    }

    public boolean hasUserId(UserId userId) {
        return this.userId.equals(userId);
    }
}
