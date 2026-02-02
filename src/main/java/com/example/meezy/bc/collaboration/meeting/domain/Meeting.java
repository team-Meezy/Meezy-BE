package com.example.meezy.bc.collaboration.meeting.domain;

import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
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
import com.example.meezy.bc.user.user.domain.vo.UserId;
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
            return; //회의를 처음 참가가 아니면서 현재 참가자면 무시
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
            return; //회의가 실행 중이 아니라면 무시
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

        if(!isActive()){
            return; //이미 종료된 미팅이라면 무시
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

    public void receiveRecording(byte[] audioData, String originalFilename, String contentType) {
        registerEvent(new RecordingReceivedEvent(
                meetingId.value(),
                audioData,
                originalFilename,
                contentType
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
