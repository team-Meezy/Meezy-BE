package com.example.meezy.bc.team.meeting.domain;

import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
import com.example.meezy.bc.team.meeting.domain.event.MeetingEndedEvent;
import com.example.meezy.bc.team.meeting.domain.event.ParticipantJoinedEvent;
import com.example.meezy.bc.team.meeting.domain.event.ParticipantLeftEvent;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingNotActiveException;
import com.example.meezy.bc.team.meeting.domain.exception.ParticipantNotFoundException;
import com.example.meezy.bc.team.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.team.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import com.example.meezy.bc.user.domain.vo.UserId;
import jakarta.persistence.*;
import lombok.*;

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
            return;
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
            return;
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
