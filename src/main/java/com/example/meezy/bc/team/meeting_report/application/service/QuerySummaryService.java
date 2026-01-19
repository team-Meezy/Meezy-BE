package com.example.meezy.bc.team.meeting_report.application.service;

import com.example.meezy.bc.team.meeting.domain.Meeting;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.team.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.team.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.team.meeting_report.application.service.dto.response.SummaryResponse;
import com.example.meezy.bc.team.meeting_report.application.service.exception.MeetingReportNotFoundException;
import com.example.meezy.bc.team.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.team.meeting_report.domain.repository.MeetingReportRepository;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuerySummaryService {

    private final MeetingReportRepository meetingReportRepository;
    private final MeetingRepository meetingRepository;

    @Transactional(readOnly = true)
    public SummaryResponse findByMeetingId(UUID teamId, UUID meetingId) {
        Meeting meeting = meetingRepository.findByMeetingId_Value(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        meeting.validateBelongsToTeam(TeamId.of(teamId));

        MeetingReport report = meetingReportRepository.findByMeetingId_Value(meetingId)
                .orElseThrow(MeetingReportNotFoundException::new);

        return SummaryResponse.from(report.getSummary(), teamId);
    }

    @Transactional(readOnly = true)
    public List<SummaryResponse> findAllByTeamId(UUID teamId) {
        List<MeetingId> meetingIds = meetingRepository.findAllByTeamId(TeamId.of(teamId)).stream()
                .map(Meeting::getMeetingId)
                .toList();

        return meetingReportRepository.findAllByMeetingIdIn(meetingIds).stream()
                .map(report -> SummaryResponse.from(report.getSummary(), teamId))
                .toList();
    }
}
