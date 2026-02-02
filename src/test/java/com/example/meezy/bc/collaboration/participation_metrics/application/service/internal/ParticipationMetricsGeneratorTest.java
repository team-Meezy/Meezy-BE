package com.example.meezy.bc.collaboration.participation_metrics.application.service.internal;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.participation_metrics.application.port.out.ParticipationCounterPort;
import com.example.meezy.bc.collaboration.participation_metrics.domain.ParticipationMetrics;
import com.example.meezy.bc.collaboration.participation_metrics.domain.repository.ParticipationMetricsRepository;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParticipationMetricsGenerator 테스트")
class ParticipationMetricsGeneratorTest {

    @Mock
    private ParticipationCounterPort participationCounterPort;

    @Mock
    private ParticipationMetricsRepository participationMetricsRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private ParticipationMetricsGenerator participationMetricsGenerator;

    private UUID meetingIdValue;
    private TeamId teamId;
    private UserId hostUserId;
    private Meeting meeting;
    private Team team;

    @BeforeEach
    void setUp() {
        meetingIdValue = UUID.randomUUID();
        teamId = TeamId.newId();
        hostUserId = UserId.newId();

        meeting = Meeting.start(teamId, hostUserId);
        meeting.end();

        team = Team.create("테스트팀", "http://image.url", hostUserId);
    }

    @Test
    @DisplayName("회의 종료 시 참여 지표를 생성하고 저장한다")
    void generate_creates_and_saves_participation_metrics() {
        given(participationMetricsRepository.existsByMeetingId(any(MeetingId.class))).willReturn(false);
        given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(meeting));
        given(teamRepository.findByTeamId_Value(meeting.getTeamId().value())).willReturn(Optional.of(team));

        Map<UUID, Integer> voiceCounts = new HashMap<>();
        voiceCounts.put(hostUserId.value(), 10);

        Map<UUID, Integer> chatCounts = new HashMap<>();
        chatCounts.put(hostUserId.value(), 3);

        given(participationCounterPort.getAllVoiceCounts(meetingIdValue)).willReturn(voiceCounts);
        given(participationCounterPort.getAllChatCounts(meetingIdValue)).willReturn(chatCounts);

        participationMetricsGenerator.generate(meetingIdValue);

        ArgumentCaptor<ParticipationMetrics> captor = ArgumentCaptor.forClass(ParticipationMetrics.class);
        verify(participationMetricsRepository).save(captor.capture());

        ParticipationMetrics savedMetrics = captor.getValue();
        assertThat(savedMetrics.getTeamId()).isEqualTo(meeting.getTeamId());
        assertThat(savedMetrics.getParticipantMetrics()).hasSize(1);
    }

    @Test
    @DisplayName("생성 후 Redis 데이터를 정리한다")
    void generate_clears_redis_data_after_save() {
        given(participationMetricsRepository.existsByMeetingId(any(MeetingId.class))).willReturn(false);
        given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(meeting));
        given(teamRepository.findByTeamId_Value(meeting.getTeamId().value())).willReturn(Optional.of(team));
        given(participationCounterPort.getAllVoiceCounts(meetingIdValue)).willReturn(new HashMap<>());
        given(participationCounterPort.getAllChatCounts(meetingIdValue)).willReturn(new HashMap<>());

        participationMetricsGenerator.generate(meetingIdValue);

        verify(participationCounterPort).clearMeetingData(meetingIdValue);
    }

    @Test
    @DisplayName("이미 참여 지표가 존재하면 생성하지 않는다")
    void generate_skips_when_already_exists() {
        given(participationMetricsRepository.existsByMeetingId(any(MeetingId.class))).willReturn(true);

        participationMetricsGenerator.generate(meetingIdValue);

        verify(participationMetricsRepository, never()).save(any());
        verify(participationCounterPort, never()).clearMeetingData(any());
    }
}
