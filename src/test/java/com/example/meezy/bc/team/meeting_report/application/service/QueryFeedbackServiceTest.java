package com.example.meezy.bc.team.meeting_report.application.service;

import com.example.meezy.bc.team.meeting.domain.Meeting;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.team.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.team.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.team.meeting_report.application.service.dto.response.FeedbackResponse;
import com.example.meezy.bc.team.meeting_report.application.service.exception.MeetingReportNotFoundException;
import com.example.meezy.bc.team.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.team.meeting_report.domain.repository.MeetingReportRepository;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import com.example.meezy.bc.user.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("QueryFeedbackService 테스트")
class QueryFeedbackServiceTest {

    @Mock
    private MeetingReportRepository meetingReportRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @InjectMocks
    private QueryFeedbackService queryFeedbackService;

    private UUID teamIdValue;
    private TeamId teamId;
    private UUID meetingIdValue;
    private MeetingId meetingId;
    private Meeting meeting;
    private MeetingReport report;

    @BeforeEach
    void setUp() {
        teamIdValue = UUID.randomUUID();
        teamId = TeamId.of(teamIdValue);
        meetingIdValue = UUID.randomUUID();
        meetingId = MeetingId.of(meetingIdValue);
        meeting = Meeting.start(teamId, UserId.newId());
        report = MeetingReport.create(meetingId, "요약 내용", "피드백 내용");
    }

    @Nested
    @DisplayName("단일 조회")
    class FindByMeetingIdTest {

        @Test
        @DisplayName("meetingId로 피드백을 조회할 수 있다")
        void findByMeetingId_returns_feedback() {
            given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(meeting));
            given(meetingReportRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(report));

            FeedbackResponse response = queryFeedbackService.findByMeetingId(teamIdValue, meetingIdValue);

            assertThat(response).isNotNull();
            assertThat(response.content()).isEqualTo("피드백 내용");
            assertThat(response.teamId()).isEqualTo(teamIdValue);
        }

        @Test
        @DisplayName("회의가 존재하지 않으면 예외가 발생한다")
        void findByMeetingId_throws_when_meeting_not_found() {
            given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.empty());

            assertThatThrownBy(() -> queryFeedbackService.findByMeetingId(teamIdValue, meetingIdValue))
                    .isInstanceOf(MeetingNotFoundException.class);
        }

        @Test
        @DisplayName("리포트가 존재하지 않으면 예외가 발생한다")
        void findByMeetingId_throws_when_report_not_found() {
            given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(meeting));
            given(meetingReportRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.empty());

            assertThatThrownBy(() -> queryFeedbackService.findByMeetingId(teamIdValue, meetingIdValue))
                    .isInstanceOf(MeetingReportNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("전체 조회")
    class FindAllByTeamIdTest {

        @Test
        @DisplayName("팀의 모든 피드백을 조회할 수 있다")
        void findAllByTeamId_returns_all_feedbacks() {
            Meeting meeting2 = Meeting.start(teamId, UserId.newId());
            MeetingReport report2 = MeetingReport.create(meeting2.getMeetingId(), "요약 2", "피드백 2");

            given(meetingRepository.findAllByTeamId(any(TeamId.class))).willReturn(List.of(meeting, meeting2));
            given(meetingReportRepository.findAllByMeetingIdIn(any())).willReturn(List.of(report, report2));

            List<FeedbackResponse> responses = queryFeedbackService.findAllByTeamId(teamIdValue);

            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("리포트가 없으면 빈 리스트를 반환한다")
        void findAllByTeamId_returns_empty_when_no_reports() {
            given(meetingRepository.findAllByTeamId(any(TeamId.class))).willReturn(List.of(meeting));
            given(meetingReportRepository.findAllByMeetingIdIn(any())).willReturn(List.of());

            List<FeedbackResponse> responses = queryFeedbackService.findAllByTeamId(teamIdValue);

            assertThat(responses).isEmpty();
        }
    }
}
