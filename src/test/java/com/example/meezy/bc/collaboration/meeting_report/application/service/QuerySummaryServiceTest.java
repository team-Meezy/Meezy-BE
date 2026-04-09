package com.example.meezy.bc.collaboration.meeting_report.application.service;

import com.example.meezy.bc.collaboration.meeting.domain.Meeting;
import com.example.meezy.bc.collaboration.meeting.domain.exception.MeetingNotFoundException;
import com.example.meezy.bc.collaboration.meeting.domain.exception.NotTeamMemberException;
import com.example.meezy.bc.collaboration.meeting.domain.repository.MeetingRepository;
import com.example.meezy.bc.collaboration.meeting.domain.vo.MeetingId;
import com.example.meezy.bc.collaboration.meeting_report.application.service.dto.response.SummaryResponse;
import com.example.meezy.bc.collaboration.meeting_report.application.service.exception.MeetingReportNotFoundException;
import com.example.meezy.bc.collaboration.meeting_report.domain.MeetingReport;
import com.example.meezy.bc.collaboration.meeting_report.domain.repository.MeetingReportRepository;
import com.example.meezy.bc.collaboration.team.domain.repository.TeamRepository;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.sharedkernel.user.AuthenticatedUser;
import com.example.meezy.bc.sharedkernel.user.CurrentUserQuery;
import com.example.meezy.bc.user.user.domain.vo.UserId;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuerySummaryService 테스트")
class QuerySummaryServiceTest {

    @Mock
    private MeetingReportRepository meetingReportRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    @InjectMocks
    private QuerySummaryService querySummaryService;

    private UUID teamIdValue;
    private TeamId teamId;
    private UUID meetingIdValue;
    private MeetingId meetingId;
    private Meeting meeting;
    private MeetingReport report;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        teamIdValue = UUID.randomUUID();
        teamId = TeamId.of(teamIdValue);
        meeting = Meeting.start(teamId, UserId.newId());
        meetingId = meeting.getMeetingId();
        meetingIdValue = meetingId.value();
        report = MeetingReport.create(meetingId, "요약 내용", "피드백 내용");
        authenticatedUser = AuthenticatedUser.builder()
                .userId(UserId.newId())
                .accountId("summary-user")
                .name("Summary User")
                .build();
    }

    @Nested
    @DisplayName("단일 조회")
    class FindByMeetingIdTest {

        @Test
        @DisplayName("meetingId로 요약을 조회할 수 있다")
        void findByMeetingId_returns_summary() {
            allowTeamAccess();
            given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(meeting));
            given(meetingReportRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(report));

            SummaryResponse response = querySummaryService.findByMeetingId(teamIdValue, meetingIdValue);

            assertThat(response).isNotNull();
            assertThat(response.content()).isEqualTo("요약 내용");
            assertThat(response.teamId()).isEqualTo(teamIdValue);
        }

        @Test
        @DisplayName("팀 멤버가 아니면 요약을 조회할 수 없다")
        void findByMeetingId_throws_when_not_team_member() {
            denyTeamAccess();

            assertThatThrownBy(() -> querySummaryService.findByMeetingId(teamIdValue, meetingIdValue))
                    .isInstanceOf(NotTeamMemberException.class);
        }

        @Test
        @DisplayName("회의가 존재하지 않으면 예외가 발생한다")
        void findByMeetingId_throws_when_meeting_not_found() {
            allowTeamAccess();
            given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.empty());

            assertThatThrownBy(() -> querySummaryService.findByMeetingId(teamIdValue, meetingIdValue))
                    .isInstanceOf(MeetingNotFoundException.class);
        }

        @Test
        @DisplayName("리포트가 존재하지 않으면 예외가 발생한다")
        void findByMeetingId_throws_when_report_not_found() {
            allowTeamAccess();
            given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(meeting));
            given(meetingReportRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.empty());

            assertThatThrownBy(() -> querySummaryService.findByMeetingId(teamIdValue, meetingIdValue))
                    .isInstanceOf(MeetingReportNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("전체 조회")
    class FindAllByTeamIdTest {

        @Test
        @DisplayName("팀의 모든 요약을 조회할 수 있다")
        void findAllByTeamId_returns_all_summaries() {
            allowTeamAccess();
            Meeting meeting2 = Meeting.start(teamId, UserId.newId());
            MeetingReport report2 = MeetingReport.create(meeting2.getMeetingId(), "요약 2", "피드백 2");

            given(meetingRepository.findAllByTeamId(any(TeamId.class))).willReturn(List.of(meeting, meeting2));
            given(meetingReportRepository.findAllByMeetingIdIn(any())).willReturn(List.of(report, report2));

            List<SummaryResponse> responses = querySummaryService.findAllByTeamId(teamIdValue);

            assertThat(responses).hasSize(2);
        }

        @Test
        @DisplayName("리포트가 없으면 빈 리스트를 반환한다")
        void findAllByTeamId_returns_empty_when_no_reports() {
            allowTeamAccess();
            given(meetingRepository.findAllByTeamId(any(TeamId.class))).willReturn(List.of(meeting));
            given(meetingReportRepository.findAllByMeetingIdIn(any())).willReturn(List.of());

            List<SummaryResponse> responses = querySummaryService.findAllByTeamId(teamIdValue);

            assertThat(responses).isEmpty();
        }
    }

    private void allowTeamAccess() {
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class))).willReturn(true);
    }

    private void denyTeamAccess() {
        given(currentUserQuery.currentUser()).willReturn(authenticatedUser);
        given(teamRepository.existsMemberByTeamIdAndUserId(eq(teamIdValue), any(UserId.class))).willReturn(false);
    }
}
