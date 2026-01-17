package com.example.meezy.bc.team.meeting.application.service;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.team.meeting.domain.Meeting;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingAlreadyExistsException;
import com.example.meezy.bc.team.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.team.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.team.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.team.team.domain.Team;
import com.example.meezy.bc.team.team.domain.repository.TeamRepository;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StartMeetingService {

    private final MeetingRepository meetingRepository;
    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;

    @Transactional
    public MeetingResponse start(UUID teamId) {
        Team team = findTeamOrThrow(teamId);

        team.validateLeaderPermission(currentUserQuery.currentUser().userId());

        validateNoActiveMeeting(teamId);

        Meeting meeting = Meeting.start(
                team.getTeamId(),
                currentUserQuery.currentUser().userId()
        );

        meetingRepository.save(meeting);

        return MeetingResponse.from(meeting);
    }

    private Team findTeamOrThrow(UUID teamId) {
        return teamRepository.findByTeamId_Value(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    private void validateNoActiveMeeting(UUID teamId) {
        if (meetingRepository.existsByTeamIdAndStatus(TeamId.of(teamId), MeetingStatus.ACTIVE)) {
            throw new MeetingAlreadyExistsException();
        }
    }
}
