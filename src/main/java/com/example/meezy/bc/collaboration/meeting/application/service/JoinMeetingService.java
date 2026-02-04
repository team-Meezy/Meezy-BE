package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.MeetingParticipant;
import com.example.meezy.bc.collaboration.meeting.domain.event.MeetingEvent;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
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

    @Transactional
    public MeetingResponse join(UUID teamId) {
        Meeting meeting = findActiveMeetingOrThrow(teamId);

        meeting.join(currentUserQuery.currentUser().userId());

        publishEvents(meeting);

        Map<UUID, User> users = getUsers(meeting);
        return MeetingResponse.from(meeting, users);
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

    private Meeting findActiveMeetingOrThrow(UUID teamId) {
        return meetingRepository.findByTeamIdAndStatus(TeamId.of(teamId), MeetingStatus.ACTIVE)
                .orElseThrow(MeetingNotFoundException::new);
    }
}
