package com.example.meezy.bc.collaboration.meeting.application.service;

import com.example.meezy.bc.collaboration.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.MeetingParticipant;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.user.user.domain.User;
import com.example.meezy.bc.user.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QueryMeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<MeetingResponse> findActiveMeeting(UUID teamId) {
        return meetingRepository.findByTeamIdAndStatus(TeamId.of(teamId), MeetingStatus.ACTIVE)
                .map(meeting -> MeetingResponse.from(meeting, getUsers(meeting)));
    }

    private Map<UUID, User> getUsers(Meeting meeting) {
        List<UUID> userIds = meeting.getParticipants().stream()
                .filter(MeetingParticipant::isActive)
                .map(p -> p.getUserId().value())
                .toList();

        return userRepository.findByUserId_ValueIn(userIds).stream()
                .collect(Collectors.toMap(user -> user.getUserId().value(), user -> user));
    }
}
