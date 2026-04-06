package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.collaboration.meeting.application.port.out.SignalMessagePublisher;
import com.example.meezy.bc.collaboration.meeting.application.service.dto.request.SignalMessage;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.NotTeamMemberException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.SignalRecipientNotInMeetingException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.SignalRecipientRequiredException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.SignalSenderMismatchException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.SignalSenderNotInMeetingException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.meeting.application.port.out.SignalRateLimitPort;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.user.user.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RelaySignalService {

    private final SignalMessagePublisher signalMessagePublisher;
    private final CurrentUserQuery currentUserQuery;
    private final TeamRepository teamRepository;
    private final MeetingRepository meetingRepository;
    private final SignalRateLimitPort signalRateLimitPort;

    public void relay(UUID teamId, SignalMessage message) {
        UserId currentUserId = currentUserQuery.currentUser().userId();

        signalRateLimitPort.validate(currentUserId.value(), teamId);

        validateSenderIdentity(currentUserId, message.fromUserId());
        validateTeamMembership(teamId, currentUserId);

        Meeting meeting = findActiveMeetingOrThrow(teamId);
        validateSenderIsParticipant(meeting, currentUserId.value());
        validateRecipientIsParticipant(meeting, message.toUserId());

        signalMessagePublisher.publish(teamId, message);
    }

    private void validateSenderIdentity(UserId currentUserId, UUID fromUserId) {
        if (!currentUserId.value().equals(fromUserId)) {
            throw new SignalSenderMismatchException();
        }
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

    private void validateSenderIsParticipant(Meeting meeting, UUID senderUserId) {
        if (!meeting.getActiveParticipantUserIds().contains(senderUserId)) {
            throw new SignalSenderNotInMeetingException();
        }
    }

    private void validateRecipientIsParticipant(Meeting meeting, UUID toUserId) {
        if (toUserId == null) {
            throw new SignalRecipientRequiredException();
        }
        if (!meeting.getActiveParticipantUserIds().contains(toUserId)) {
            throw new SignalRecipientNotInMeetingException();
        }
    }
}
