package com.example.meezy.bc.team.meeting.application.service;

import com.example.meezy.bc.team.meeting.application.service.dto.response.MeetingResponse;
import com.example.meezy.bc.team.meeting.domain.Meeting;
import com.example.meezy.bc.team.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.team.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QueryMeetingService {

    private final MeetingRepository meetingRepository;

    @Transactional(readOnly = true)
    public Optional<MeetingResponse> findActiveMeeting(UUID teamId) {
        return meetingRepository.findByTeamIdAndStatus(TeamId.of(teamId), MeetingStatus.ACTIVE)
                .map(MeetingResponse::from);
    }
}
