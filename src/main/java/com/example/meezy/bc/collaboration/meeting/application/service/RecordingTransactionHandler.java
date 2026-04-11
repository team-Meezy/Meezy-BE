package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.event.RecordingReceivedEvent;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RecordingTransactionHandler {

    private final MeetingRepository meetingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public void validateMeetingOwnership(UUID teamId, UUID meetingId) {
        Meeting meeting = meetingRepository.findByMeetingId_Value(meetingId)
                .orElseThrow(MeetingNotFoundException::new);
        meeting.validateBelongsToTeam(TeamId.of(teamId));
    }

    @Transactional
    public void saveRecording(UUID teamId, UUID meetingId, String s3Key, String title) {
        Meeting meeting = meetingRepository.findByMeetingId_Value(meetingId)
                .orElseThrow(MeetingNotFoundException::new);
        meeting.validateBelongsToTeam(TeamId.of(teamId));
        meeting.receiveRecording(s3Key, title);
        meetingRepository.save(meeting);

        meeting.pullDomainEvents().forEach(event -> {
            if (event instanceof RecordingReceivedEvent recordingEvent) {
                eventPublisher.publishEvent(recordingEvent);
            }
        });
    }
}
