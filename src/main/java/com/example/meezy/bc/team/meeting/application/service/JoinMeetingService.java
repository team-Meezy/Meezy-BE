package com.example.meezy.bc.team.meeting.application.service;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.team.meeting.domain.Meeting;
import com.example.meezy.bc.team.meeting.domain.event.MeetingEvent;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.team.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.team.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JoinMeetingService {

    private final MeetingRepository meetingRepository;
    private final CurrentUserQuery currentUserQuery;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MeetingResponse join(UUID teamId) {
        Meeting meeting = findActiveMeetingOrThrow(teamId);

        meeting.join(currentUserQuery.currentUser().userId());

        publishEvents(meeting);

        return MeetingResponse.from(meeting);
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
