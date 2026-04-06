package com.example.meezy.bc.collaboration.meeting.domain.repository;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, MeetingId> {

    Optional<Meeting> findByMeetingId_Value(UUID meetingId);

    Optional<Meeting> findByTeamIdAndStatus(TeamId teamId, MeetingStatus status);

    List<Meeting> findAllByTeamId(TeamId teamId);

    boolean existsByTeamIdAndStatus(TeamId teamId, MeetingStatus status);

    boolean existsByMeetingIdAndStatus(MeetingId meetingId, MeetingStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Meeting m WHERE m.teamId = :teamId AND m.status = :status")
    Optional<Meeting> findByTeamIdAndStatusForUpdate(
            @Param("teamId") TeamId teamId,
            @Param("status") MeetingStatus status);
}
