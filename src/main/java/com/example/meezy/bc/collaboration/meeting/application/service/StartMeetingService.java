package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.port.out.IceServerQueryPort;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingAlreadyExistsException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.team.application.service.exception.TeamNotFoundException;
import com.example.meezy.bc.collaboration.team.domain.Team;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StartMeetingService {

    private final MeetingRepository meetingRepository;
    private final TeamRepository teamRepository;
    private final CurrentUserQuery currentUserQuery;
    private final UserRepository userRepository;
    private final IceServerQueryPort iceServerQueryPort;

    @Transactional
    public MeetingResponse start(UUID teamId) {
        Team team = lockTeamOrThrow(teamId);

        team.validateLeaderPermission(currentUserQuery.currentUser().userId());

        validateNoActiveMeeting(teamId);

        Meeting meeting = Meeting.start(
                team.getTeamId(),
                currentUserQuery.currentUser().userId()
        );

        meetingRepository.save(meeting);

        UUID hostUserId = currentUserQuery.currentUser().userId().value();
        Map<UUID, User> users = userRepository.findByUserId_ValueIn(List.of(hostUserId)).stream()
                .collect(Collectors.toMap(user -> user.getUserId().value(), user -> user));

        var iceServers = iceServerQueryPort.getIceServers();
        return MeetingResponse.from(meeting, users, iceServers);
    }

    private Team lockTeamOrThrow(UUID teamId) {
        return teamRepository.findByTeamIdForUpdate(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    private void validateNoActiveMeeting(UUID teamId) {
        if (meetingRepository.existsByTeamIdAndStatus(TeamId.of(teamId), MeetingStatus.ACTIVE)) {
            throw new MeetingAlreadyExistsException();
        }
    }
}
