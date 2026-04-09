package com.example.meezy.bc.collaboration.participation_metrics.application.service.internal;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.participation_metrics.application.port.out.ParticipationCounterPort;
import com.example.meezy.bc.collaboration.participation_metrics.domain.ParticipationMetrics;
import com.example.meezy.bc.collaboration.participation_metrics.domain.repository.ParticipationMetricsRepository;
import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.TeamMember;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationMetricsGenerator {

    private final ParticipationCounterPort participationCounterPort;
    private final ParticipationMetricsRepository participationMetricsRepository;
    private final MeetingRepository meetingRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public void generate(UUID meetingId) {
        if (participationMetricsRepository.existsByMeetingId(MeetingId.of(meetingId))) {
            log.warn("Skipping participation metrics generation because metrics already exist: meetingId={}", meetingId);
            return;
        }

        Meeting meeting = meetingRepository.findByMeetingId_Value(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        Team team = teamRepository.findByTeamId_Value(meeting.getTeamId().value())
                .orElseThrow(TeamNotFoundException::new);

        List<UserId> allMemberIds = team.getMembers().stream()
                .map(TeamMember::getUserId)
                .toList();

        Map<UUID, Long> connectionDurations = calculateConnectionDurations(meeting);
        Map<UUID, Integer> voiceCounts = participationCounterPort.getAllVoiceCounts(meetingId);
        Map<UUID, Integer> chatCounts = participationCounterPort.getAllChatCounts(meetingId);
        long meetingDurationSeconds = calculateMeetingDuration(meeting);

        log.info("Loaded participation counters for metrics generation: meetingId={}, meetingDurationSeconds={}, connectionDurations={}, voiceCounts={}, chatCounts={}",
                meetingId, meetingDurationSeconds, connectionDurations, voiceCounts, chatCounts);

        if (voiceCounts.isEmpty() && chatCounts.isEmpty()) {
            log.warn("No participation counters found in Redis when generating metrics: meetingId={}", meetingId);
        }

        ParticipationMetrics metrics = ParticipationMetrics.create(
                meeting.getMeetingId(),
                meeting.getTeamId(),
                meetingDurationSeconds,
                allMemberIds,
                connectionDurations,
                voiceCounts,
                chatCounts
        );

        participationMetricsRepository.save(metrics);

        log.info("Generated participation metrics: meetingId={}, teamId={}, participantCount={}, voiceCounterUsers={}, chatCounterUsers={}",
                meetingId, meeting.getTeamId().value(), allMemberIds.size(), voiceCounts.size(), chatCounts.size());
    }

    public void clearRedisData(UUID meetingId) {
        log.info("Clearing participation Redis data after metrics generation: meetingId={}", meetingId);
        participationCounterPort.clearMeetingData(meetingId);
    }

    private Map<UUID, Long> calculateConnectionDurations(Meeting meeting) {
        return meeting.getParticipants().stream()
                .collect(Collectors.toMap(
                        participant -> participant.getUserId().value(),
                        participant -> participant.getTotalConnectionSeconds(meeting.getEndedAt()),
                        Long::sum
                ));
    }

    private long calculateMeetingDuration(Meeting meeting) {
        if (meeting.getStartedAt() == null || meeting.getEndedAt() == null) {
            return 0L;
        }
        return Duration.between(meeting.getStartedAt(), meeting.getEndedAt()).getSeconds();
    }
}
