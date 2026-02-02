package com.example.meezy.bc.collaboration.participation_metrics.domain.repository;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.participation_metrics.domain.ParticipationMetrics;
import com.example.meezy.bc.collaboration.participation_metrics.domain.vo.ParticipationMetricsId;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipationMetricsRepository extends JpaRepository<ParticipationMetrics, ParticipationMetricsId> {

    Optional<ParticipationMetrics> findByMeetingId(MeetingId meetingId);

    Optional<ParticipationMetrics> findByMeetingId_Value(UUID meetingId);

    boolean existsByMeetingId(MeetingId meetingId);

    @Query("""
            SELECT pm.participationRate.value
            FROM ParticipantMetric pm
            JOIN pm.participationMetrics pms
            WHERE pms.teamId = :teamId
            AND pm.userId = :userId
            ORDER BY pms.createdAt DESC
            LIMIT 30
            """)
    List<Double> findRecentRatesByTeamIdAndUserId(
            @Param("teamId") TeamId teamId,
            @Param("userId") UserId userId
    );

    @Query("""
            SELECT pm
            FROM ParticipationMetrics pm
            JOIN FETCH pm.participantMetrics
            WHERE pm.meetingId = :meetingId
            """)
    Optional<ParticipationMetrics> findByMeetingIdWithParticipants(@Param("meetingId") MeetingId meetingId);
}
