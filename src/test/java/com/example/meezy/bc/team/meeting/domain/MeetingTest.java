package com.example.meezy.bc.team.meeting.domain;

import com.example.meezy.bc.team.meeting.domain.event.MeetingEndedEvent;
import com.example.meezy.bc.team.meeting.domain.event.ParticipantJoinedEvent;
import com.example.meezy.bc.team.meeting.domain.event.ParticipantLeftEvent;
import com.example.meezy.bc.team.meeting.domain.event.RecordingReceivedEvent;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingNotActiveException;
import com.example.meezy.bc.team.meeting.domain.exception.MeetingNotBelongsToTeamException;
import com.example.meezy.bc.team.meeting.domain.exception.ParticipantNotFoundException;
import com.example.meezy.bc.team.meeting.domain.type.MeetingStatus;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import com.example.meezy.bc.user.domain.vo.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Meeting 도메인 테스트")
class MeetingTest {

    private final TeamId teamId = TeamId.newId();
    private final UserId hostUserId = UserId.newId();

    @Nested
    @DisplayName("회의 시작")
    class StartTest {

        @Test
        @DisplayName("회의를 시작하면 ACTIVE 상태가 된다")
        void start_creates_active_meeting() {
            Meeting meeting = Meeting.start(teamId, hostUserId);

            assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.ACTIVE);
            assertThat(meeting.isActive()).isTrue();
            assertThat(meeting.getTeamId()).isEqualTo(teamId);
            assertThat(meeting.getHostUserId()).isEqualTo(hostUserId);
            assertThat(meeting.getMeetingId()).isNotNull();
            assertThat(meeting.getStartedAt()).isNotNull();
        }

        @Test
        @DisplayName("회의를 시작하면 호스트가 참가자로 추가된다")
        void start_adds_host_as_participant() {
            Meeting meeting = Meeting.start(teamId, hostUserId);

            assertThat(meeting.getActiveParticipantCount()).isEqualTo(1);
            assertThat(meeting.getActiveParticipantUserIds()).contains(hostUserId.value());
        }
    }

    @Nested
    @DisplayName("회의 참가")
    class JoinTest {

        @Test
        @DisplayName("새로운 참가자가 회의에 참가할 수 있다")
        void join_adds_new_participant() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            UserId newUserId = UserId.newId();

            meeting.join(newUserId);

            assertThat(meeting.getActiveParticipantCount()).isEqualTo(2);
            assertThat(meeting.getActiveParticipantUserIds()).contains(newUserId.value());
        }

        @Test
        @DisplayName("회의 참가 시 ParticipantJoinedEvent가 발생한다")
        void join_registers_event() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            UserId newUserId = UserId.newId();

            meeting.join(newUserId);

            assertThat(meeting.pullDomainEvents())
                    .hasAtLeastOneElementOfType(ParticipantJoinedEvent.class);
        }

        @Test
        @DisplayName("이미 참가 중인 사용자가 참가하면 무시된다")
        void join_ignores_already_active_participant() {
            Meeting meeting = Meeting.start(teamId, hostUserId);

            meeting.join(hostUserId);

            assertThat(meeting.getActiveParticipantCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("퇴장한 참가자가 다시 참가할 수 있다")
        void join_allows_rejoin() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            UserId userId = UserId.newId();
            meeting.join(userId);
            meeting.leave(userId);

            meeting.join(userId);

            assertThat(meeting.getActiveParticipantCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("종료된 회의에는 참가할 수 없다")
        void join_throws_when_meeting_not_active() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            meeting.end();

            assertThatThrownBy(() -> meeting.join(UserId.newId()))
                    .isInstanceOf(MeetingNotActiveException.class);
        }
    }

    @Nested
    @DisplayName("회의 퇴장")
    class LeaveTest {

        @Test
        @DisplayName("참가자가 퇴장할 수 있다")
        void leave_removes_participant() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            UserId userId = UserId.newId();
            meeting.join(userId);

            meeting.leave(userId);

            assertThat(meeting.getActiveParticipantCount()).isEqualTo(1);
            assertThat(meeting.getActiveParticipantUserIds()).doesNotContain(userId.value());
        }

        @Test
        @DisplayName("퇴장 시 ParticipantLeftEvent가 발생한다")
        void leave_registers_event() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            UserId userId = UserId.newId();
            meeting.join(userId);
            meeting.pullDomainEvents();

            meeting.leave(userId);

            assertThat(meeting.pullDomainEvents())
                    .hasAtLeastOneElementOfType(ParticipantLeftEvent.class);
        }

        @Test
        @DisplayName("마지막 참가자가 퇴장하면 회의가 종료된다")
        void leave_ends_meeting_when_last_participant_leaves() {
            Meeting meeting = Meeting.start(teamId, hostUserId);

            meeting.leave(hostUserId);

            assertThat(meeting.isActive()).isFalse();
            assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.ENDED);
        }

        @Test
        @DisplayName("존재하지 않는 참가자가 퇴장하면 예외가 발생한다")
        void leave_throws_when_participant_not_found() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            UserId unknownUserId = UserId.newId();

            assertThatThrownBy(() -> meeting.leave(unknownUserId))
                    .isInstanceOf(ParticipantNotFoundException.class);
        }

        @Test
        @DisplayName("종료된 회의에서는 퇴장할 수 없다")
        void leave_throws_when_meeting_not_active() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            meeting.end();

            assertThatThrownBy(() -> meeting.leave(hostUserId))
                    .isInstanceOf(MeetingNotActiveException.class);
        }

        @Test
        @DisplayName("이미 퇴장한 참가자가 다시 퇴장하면 무시된다")
        void leave_ignores_already_left_participant() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            UserId userId = UserId.newId();
            meeting.join(userId);
            meeting.leave(userId);
            meeting.join(userId);
            meeting.pullDomainEvents();

            meeting.leave(userId);
            meeting.leave(userId);

            assertThat(meeting.getActiveParticipantCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("회의 종료")
    class EndTest {

        @Test
        @DisplayName("회의를 종료하면 ENDED 상태가 된다")
        void end_changes_status_to_ended() {
            Meeting meeting = Meeting.start(teamId, hostUserId);

            meeting.end();

            assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.ENDED);
            assertThat(meeting.isActive()).isFalse();
            assertThat(meeting.getEndedAt()).isNotNull();
        }

        @Test
        @DisplayName("회의 종료 시 모든 참가자가 퇴장 처리된다")
        void end_marks_all_participants_as_left() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            meeting.join(UserId.newId());
            meeting.join(UserId.newId());

            meeting.end();

            assertThat(meeting.getActiveParticipantCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("회의 종료 시 MeetingEndedEvent가 발생한다")
        void end_registers_event() {
            Meeting meeting = Meeting.start(teamId, hostUserId);

            meeting.end();

            assertThat(meeting.pullDomainEvents())
                    .hasAtLeastOneElementOfType(MeetingEndedEvent.class);
        }

        @Test
        @DisplayName("이미 종료된 회의를 다시 종료하면 무시된다")
        void end_ignores_already_ended_meeting() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            meeting.end();
            meeting.pullDomainEvents();

            meeting.end();

            assertThat(meeting.pullDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("팀 소속 검증")
    class ValidateBelongsToTeamTest {

        @Test
        @DisplayName("회의가 해당 팀에 속하면 검증을 통과한다")
        void validateBelongsToTeam_passes_when_belongs() {
            Meeting meeting = Meeting.start(teamId, hostUserId);

            meeting.validateBelongsToTeam(teamId);
        }

        @Test
        @DisplayName("회의가 해당 팀에 속하지 않으면 예외가 발생한다")
        void validateBelongsToTeam_throws_when_not_belongs() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            TeamId otherTeamId = TeamId.newId();

            assertThatThrownBy(() -> meeting.validateBelongsToTeam(otherTeamId))
                    .isInstanceOf(MeetingNotBelongsToTeamException.class);
        }
    }

    @Nested
    @DisplayName("녹음 수신")
    class ReceiveRecordingTest {

        @Test
        @DisplayName("녹음 수신 시 RecordingReceivedEvent가 발생한다")
        void receiveRecording_registers_event() {
            Meeting meeting = Meeting.start(teamId, hostUserId);
            byte[] audioData = "test audio data".getBytes();

            meeting.receiveRecording(audioData, "test.mp3", "audio/mpeg");

            assertThat(meeting.pullDomainEvents())
                    .hasAtLeastOneElementOfType(RecordingReceivedEvent.class);
        }
    }
}
