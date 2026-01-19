package com.example.meezy.bc.team.meeting_report.domain.repository;

import com.example.meezy.bc.team.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.team.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.team.meeting_report.domain.vo.MeetingReportId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeetingReportRepository extends JpaRepository<MeetingReport, MeetingReportId> {

    Optional<MeetingReport> findByMeetingReportId_Value(UUID meetingReportIdValue);

    Optional<MeetingReport> findByMeetingId(MeetingId meetingId);

    Optional<MeetingReport> findByMeetingId_Value(UUID meetingId);

    List<MeetingReport> findAllByMeetingIdIn(List<MeetingId> meetingIds);

    boolean existsByMeetingId(MeetingId meetingId);
}
