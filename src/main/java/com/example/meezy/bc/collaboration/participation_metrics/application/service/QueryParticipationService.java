package com.example.meezy.bc.collaboration.participation_metrics.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.dto.response.MeetingParticipationResponse;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.dto.response.ParticipationResponse;
import com.example.meezy.bc.collaboration.participation_metrics.application.service.exception.ParticipationMetricsNotFoundException;
import com.example.meezy.bc.collaboration.participation_metrics.domain.ParticipationMetrics;
import com.example.meezy.bc.collaboration.participation_metrics.domain.repository.ParticipationMetricsRepository;
import com.example.meezy.bc.collaboration.participation_metrics.domain.vo.ParticipationRate;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QueryParticipationService {

    private final ParticipationMetricsRepository participationMetricsRepository;
    private final CurrentUserQuery currentUserQuery;

    public ParticipationResponse getParticipation(UUID teamId, UUID meetingId) {

        UserId userId = currentUserQuery.currentUser().userId();

        ParticipationMetrics metrics = participationMetricsRepository
                .findByMeetingIdWithParticipants(MeetingId.of(meetingId))
                .orElseThrow(ParticipationMetricsNotFoundException::new);

        ParticipationRate currentRate = metrics.getParticipationRateByUserId(userId); //현지 회의 참여율 계산

        List<Double> recentRates = participationMetricsRepository
                .findRecentRatesByTeamIdAndUserId(TeamId.of(teamId), userId); //최근 회의 30개 조회

        double averageRate = calculateAverage(recentRates); //30개 회의의 평균 시간

        return ParticipationResponse.of(
                meetingId,
                userId.value(),
                currentRate.getValue(),
                averageRate,
                recentRates.size()
        );
    }

    public MeetingParticipationResponse getMeetingParticipation(UUID meetingId) {
        ParticipationMetrics metrics = participationMetricsRepository
                .findByMeetingIdWithParticipants(MeetingId.of(meetingId))
                .orElseThrow(ParticipationMetricsNotFoundException::new);

        return MeetingParticipationResponse.from(metrics);
    }

    public double getAverageRate(UUID teamId, UUID userId) { //추후 통계 데이터 제공용 API
        List<Double> recentRates = participationMetricsRepository
                .findRecentRatesByTeamIdAndUserId(TeamId.of(teamId), UserId.of(userId));

        return calculateAverage(recentRates);
    }

    private double calculateAverage(List<Double> rates) {
        if (rates.isEmpty()) {
            return 0.0;
        }
        return rates.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
