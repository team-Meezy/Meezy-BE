package com.example.meezy.bc.collaboration.meeting.domain;

import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEndedEvent;
import com.example.meezy.bc.collaboration.meeting.domain.event.ParticipantJoinedEvent;
import com.example.meezy.bc.collaboration.meeting.domain.event.ParticipantLeftEvent;
import com.example.meezy.bc.collaboration.meeting.domain.event.RecordingReceivedEvent;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotActiveException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotBelongsToTeamException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.ParticipantNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_meeting")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Meeting extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "meeting_id"))
    private MeetingId meetingId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "team_id"))
    private TeamId teamId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "host_user_id"))
    private UserId hostUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "meeting",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MeetingParticipant> participants = new ArrayList<>();

    public static Meeting start(TeamId teamId, UserId hostUserId) {
        Meeting meeting = Meeting.builder()
                .meetingId(MeetingId.newId())
                .teamId(teamId)
                .hostUserId(hostUserId)
                .status(MeetingStatus.ACTIVE)
                .startedAt(LocalDateTime.now())
                .build();

        meeting.addParticipant(hostUserId);
        return meeting;
    }

    public void join(UserId userId) {
        validateActive();

        MeetingParticipant existingParticipant = findParticipantByUserId(userId);

        if (existingParticipant != null && existingParticipant.isActive()) {
            return; //?뚯쓽瑜?泥섏쓬 李멸?媛 ?꾨땲硫댁꽌 ?꾩옱 李멸??먮㈃ 臾댁떆
        }

        List<UUID> existingParticipantIds = getActiveParticipantUserIds();

        if (existingParticipant != null) {
            existingParticipant.rejoin();
        } else {
            addParticipant(userId);
        }

        registerEvent(new ParticipantJoinedEvent(
                teamId.value(),
                meetingId.value(),
                userId.value(),
                existingParticipantIds
        ));
    }

    public void leave(UserId userId) {
        validateActive();

        MeetingParticipant participant = findParticipantByUserIdOrThrow(userId);

        if (!participant.isActive()) {
            return; //?뚯쓽媛 ?ㅽ뻾 以묒씠 ?꾨땲?쇰㈃ 臾댁떆
        }

        participant.leave();

        registerEvent(new ParticipantLeftEvent(
                teamId.value(),
                meetingId.value(),
                userId.value()
        ));

        if (getActiveParticipantCount() == 0) {
            end();
        }
    }

    public void end() {
        if (!isActive()) {
            return; //?대? 醫낅즺??誘명똿?대씪硫?臾댁떆
        }
        this.status = MeetingStatus.ENDED;
        this.endedAt = LocalDateTime.now();

        participants.stream()
                .filter(MeetingParticipant::isActive)
                .forEach(MeetingParticipant::leave);

        registerEvent(new MeetingEndedEvent(
                teamId.value(),
                meetingId.value()
        ));
    }

    public void receiveRecording(String s3Key, String title) {
        if (s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("s3Key??null?닿굅??鍮?媛믪씪 ???놁뒿?덈떎.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title??null?닿굅??鍮?媛믪씪 ???놁뒿?덈떎.");
        }
        registerEvent(new RecordingReceivedEvent(
                meetingId.value(),
                s3Key,
                title
        ));
    }

    public boolean belongsToTeam(TeamId teamId) {
        return this.teamId.equals(teamId);
    }

    public void validateBelongsToTeam(TeamId teamId) {
        if (!belongsToTeam(teamId)) {
            throw new MeetingNotBelongsToTeamException();
        }
    }

    public boolean isActive() {
        return this.status == MeetingStatus.ACTIVE;
    }

    public long getActiveParticipantCount() {
        return participants.stream()
                .filter(MeetingParticipant::isActive)
                .count();
    }

    public List<UUID> getActiveParticipantUserIds() {
        return participants.stream()
                .filter(MeetingParticipant::isActive)
                .map(MeetingParticipant::getUserId)
                .map(UserId::value)
                .toList();
    }

    private void addParticipant(UserId userId) {
        MeetingParticipant participant = MeetingParticipant.create(userId, this);
        participants.add(participant);
    }

    private MeetingParticipant findParticipantByUserId(UserId userId) {
        return participants.stream()
                .filter(p -> p.hasUserId(userId))
                .findFirst()
                .orElse(null);
    }

    private MeetingParticipant findParticipantByUserIdOrThrow(UserId userId) {
        return participants.stream()
                .filter(p -> p.hasUserId(userId))
                .findFirst()
                .orElseThrow(ParticipantNotFoundException::new);
    }

    private void validateActive() {
        if (!isActive()) {
            throw new MeetingNotActiveException();
        }
    }
}
