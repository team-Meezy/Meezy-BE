package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.service.exception.EmptyRecordingException;
import com.example.meezy.bc.collaboration.meeting.application.service.exception.InvalidRecordingFormatException;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.event.RecordingReceivedEvent;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.file.AudioStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiveRecordingService {

    private final MeetingRepository meetingRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AudioStoragePort audioStoragePort;

    @Transactional
    public void receive(UUID teamId, UUID meetingId, MultipartFile recording) {
        validateRecording(recording);

        Meeting meeting = meetingRepository.findByMeetingId_Value(meetingId)
                .orElseThrow(MeetingNotFoundException::new);

        meeting.validateBelongsToTeam(TeamId.of(teamId));

        String s3Key = audioStoragePort.uploadAudio(recording);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    log.warn("트랜잭션 롤백으로 S3 오디오 삭제: key={}", s3Key);
                    audioStoragePort.deleteAudio(s3Key);
                }
            }
        });

        meeting.receiveRecording(s3Key);

        publishEvents(meeting);
    }

    private void validateRecording(MultipartFile recording) {
        if (recording == null || recording.isEmpty()) {
            throw new EmptyRecordingException();
        }

        String filename = recording.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".mp3")) {
            throw new InvalidRecordingFormatException();
        }

        String contentType = recording.getContentType();
        if (contentType == null || !contentType.equals("audio/mpeg")) {
            throw new InvalidRecordingFormatException();
        }
    }

    private void publishEvents(Meeting meeting) {
        meeting.pullDomainEvents().forEach(event -> {
            if (event instanceof RecordingReceivedEvent recordingEvent) {
                eventPublisher.publishEvent(recordingEvent);
            }
        });
    }
}
