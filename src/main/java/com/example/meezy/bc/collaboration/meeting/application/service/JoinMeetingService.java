package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.port.out.IceServerQueryPort;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.MeetingParticipant;
import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEvent;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.NotTeamMemberException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JoinMeetingService {

    private final MeetingRepository meetingRepository;
    private final CurrentUserQuery currentUserQuery;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final IceServerQueryPort iceServerQueryPort;
    private final TeamRepository teamRepository;

    @Transactional
    public MeetingResponse join(UUID teamId) {
        UserId currentUserId = currentUserQuery.currentUser().userId();
        validateTeamMembership(teamId, currentUserId);

        Meeting meeting = findActiveMeetingOrThrow(teamId);

        meeting.join(currentUserId);

        publishEvents(meeting);

        Map<UUID, User> users = getUsers(meeting);
        var iceServers = iceServerQueryPort.getIceServers();
        return MeetingResponse.from(meeting, users, iceServers);
    }

    private Map<UUID, User> getUsers(Meeting meeting) {
        List<UUID> userIds = meeting.getParticipants().stream()
                .filter(MeetingParticipant::isActive)
                .map(p -> p.getUserId().value())
                .toList();

        return userRepository.findByUserId_ValueIn(userIds).stream()
                .collect(Collectors.toMap(user -> user.getUserId().value(), user -> user));
    }

    private void publishEvents(Meeting meeting) {
        meeting.pullDomainEvents().forEach(event -> {
            if (event instanceof MeetingEvent meetingEvent) {
                eventPublisher.publishEvent(meetingEvent);
            }
        });
    }

    private void validateTeamMembership(UUID teamId, UserId userId) {
        if (!teamRepository.existsMemberByTeamIdAndUserId(teamId, userId)) {
            throw new NotTeamMemberException();
        }
    }

    private Meeting findActiveMeetingOrThrow(UUID teamId) {
        return meetingRepository.findByTeamIdAndStatus(TeamId.of(teamId), MeetingStatus.ACTIVE)
                .orElseThrow(MeetingNotFoundException::new);
    }
}
