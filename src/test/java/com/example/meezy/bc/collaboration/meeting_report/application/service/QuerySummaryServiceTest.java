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
@DisplayName("QuerySummaryService tests")
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
    private Meeting meeting;
    private MeetingReport report;
    private AuthenticatedUser authenticatedUser;

    @BeforeEach
    void setUp() {
        teamIdValue = UUID.randomUUID();
        teamId = TeamId.of(teamIdValue);
        meeting = Meeting.start(teamId, UserId.newId());
        meetingIdValue = meeting.getMeetingId().value();
        report = MeetingReport.create(MeetingId.of(meetingIdValue), "Sprint Review", "summary content", "feedback content");
        authenticatedUser = AuthenticatedUser.builder()
                .userId(UserId.newId())
                .accountId("summary-user")
                .name("Summary User")
                .build();
    }

    @Nested
    @DisplayName("findByMeetingId")
    class FindByMeetingIdTest {

        @Test
        @DisplayName("returns summary with shared title")
        void findByMeetingId_returns_summary() {
            allowTeamAccess();
            given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(meeting));
            given(meetingReportRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(report));

            SummaryResponse response = querySummaryService.findByMeetingId(teamIdValue, meetingIdValue);

            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("Sprint Review");
            assertThat(response.content()).isEqualTo("summary content");
            assertThat(response.teamId()).isEqualTo(teamIdValue);
        }

        @Test
        @DisplayName("throws when requester is not a team member")
        void findByMeetingId_throws_when_not_team_member() {
            denyTeamAccess();

            assertThatThrownBy(() -> querySummaryService.findByMeetingId(teamIdValue, meetingIdValue))
                    .isInstanceOf(NotTeamMemberException.class);
        }

        @Test
        @DisplayName("throws when meeting does not exist")
        void findByMeetingId_throws_when_meeting_not_found() {
            allowTeamAccess();
            given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.empty());

            assertThatThrownBy(() -> querySummaryService.findByMeetingId(teamIdValue, meetingIdValue))
                    .isInstanceOf(MeetingNotFoundException.class);
        }

        @Test
        @DisplayName("throws when report does not exist")
        void findByMeetingId_throws_when_report_not_found() {
            allowTeamAccess();
            given(meetingRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.of(meeting));
            given(meetingReportRepository.findByMeetingId_Value(meetingIdValue)).willReturn(Optional.empty());

            assertThatThrownBy(() -> querySummaryService.findByMeetingId(teamIdValue, meetingIdValue))
                    .isInstanceOf(MeetingReportNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAllByTeamId")
    class FindAllByTeamIdTest {

        @Test
        @DisplayName("returns all summaries")
        void findAllByTeamId_returns_all_summaries() {
            allowTeamAccess();
            Meeting meeting2 = Meeting.start(teamId, UserId.newId());
            MeetingReport report2 = MeetingReport.create(meeting2.getMeetingId(), "Retrospective", "summary 2", "feedback 2");

            given(meetingRepository.findAllByTeamId(any(TeamId.class))).willReturn(List.of(meeting, meeting2));
            given(meetingReportRepository.findAllByMeetingIdIn(any())).willReturn(List.of(report, report2));

            List<SummaryResponse> responses = querySummaryService.findAllByTeamId(teamIdValue);

            assertThat(responses).hasSize(2);
            assertThat(responses).extracting(SummaryResponse::title)
                    .containsExactlyInAnyOrder("Sprint Review", "Retrospective");
        }

        @Test
        @DisplayName("returns empty list when no reports exist")
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
