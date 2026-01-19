package com.example.meezy.bc.team.meeting.application.service;

import com.example.meezy.bc.team.meeting.domain.Meeting;
import com.example.meezy.bc.team.meeting.domain.event.RecordingReceivedEvent;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.team.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReceiveRecordingService {

    private final MeetingRepository meetingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void receive(UUID teamId, UUID meetingId, MultipartFile recording) {
        Meeting meeting = meetingRepository.findByMeetingId_Value(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        meeting.validateBelongsToTeam(TeamId.of(teamId));
        meeting.receiveRecording(recording);

        publishEvents(meeting);
    }

    private void publishEvents(Meeting meeting) {
        meeting.pullDomainEvents().forEach(event -> {
            if (event instanceof RecordingReceivedEvent recordingEvent) {
                eventPublisher.publishEvent(recordingEvent);
            }
        });
    }
}
