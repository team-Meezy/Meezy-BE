package com.example.meezy.bc.team.meeting.application.service;

import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.team.meeting.application.port.out.SignalMessagePublisher;
import com.example.meezy.bc.team.meeting.application.service.dto.request.SignalMessage;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.team.meeting.domain.exception.NotTeamMemberException;
import com.example.meezy.bc.team.meeting.domain.exception.SignalSenderMismatchException;
import com.example.meezy.bc.team.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.team.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.team.team.domain.repository.TeamRepository;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import com.example.meezy.bc.user.domain.vo.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RelaySignalService {

    private final SignalMessagePublisher signalMessagePublisher;
    private final CurrentUserQuery currentUserQuery;
    private final TeamRepository teamRepository;
    private final MeetingRepository meetingRepository;

    public void relay(UUID teamId, SignalMessage message) {
        UserId currentUserId = currentUserQuery.currentUser().userId();

        validateSenderIdentity(currentUserId, message.fromUserId());
        validateTeamMembership(teamId, currentUserId);
        validateActiveMeetingExists(teamId);

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

    private void validateActiveMeetingExists(UUID teamId) {
        if (!meetingRepository.existsByTeamIdAndStatus(TeamId.of(teamId), MeetingStatus.ACTIVE)) {
            throw new MeetingNotFoundException();
        }
    }
}
