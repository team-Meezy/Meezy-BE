package com.example.meezy.bc.collaboration.meeting_report.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.NotTeamMemberException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.meeting_report.application.service.dto.response.SummaryResponse;
import com.example.meezy.bc.collaboration.meeting_report.application.service.exception.MeetingReportNotFoundException;
import com.example.meezy.bc.collaboration.meeting_report.application.service.exception.ReportGenerationFailedException;
import com.example.meezy.bc.collaboration.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.collaboration.meeting_report.domain.repository.MeetingReportRepository;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
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
public class QuerySummaryService {

    private final MeetingReportRepository meetingReportRepository;
    private final MeetingRepository meetingRepository;
    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional(readOnly = true)
    public SummaryResponse findByMeetingId(UUID teamId, UUID meetingId) {
        validateTeamMembership(teamId, currentUserQuery.currentUser().userId());

        Meeting meeting = meetingRepository.findByMeetingId_Value(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        meeting.validateBelongsToTeam(TeamId.of(teamId));

        MeetingReport report = meetingReportRepository.findByMeetingId_Value(meetingId)
                .orElseThrow(MeetingReportNotFoundException::new);

        if (report.isFailed()) {
            throw new ReportGenerationFailedException();
        }
        if (!report.isCompleted() || report.getSummary() == null) {
            throw new MeetingReportNotFoundException();
        }

        return SummaryResponse.from(report, teamId);
    }

    @Transactional(readOnly = true)
    public List<SummaryResponse> findAllByTeamId(UUID teamId) {
        validateTeamMembership(teamId, currentUserQuery.currentUser().userId());

        List<MeetingId> meetingIds = meetingRepository.findAllByTeamId(TeamId.of(teamId)).stream()
                .map(Meeting::getMeetingId)
                .toList();

        return meetingReportRepository.findAllByMeetingIdIn(meetingIds).stream()
                .filter(MeetingReport::isCompleted)
                .filter(report -> report.getSummary() != null)
                .map(report -> SummaryResponse.from(report, teamId))
                .toList();
    }

    private void validateTeamMembership(UUID teamId, UserId userId) {
        if (!teamRepository.existsMemberByTeamIdAndUserId(teamId, userId)) {
            throw new NotTeamMemberException();
        }
    }
}
