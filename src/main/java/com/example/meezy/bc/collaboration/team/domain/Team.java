package com.example.meezy.bc.collaboration.team.domain;

import com.example.meezy.bc.sharedkernel.domain.AbstractAggregateRoot;
import com.example.meezy.bc.collaboration.team.domain.exception.*;
import com.example.meezy.bc.collaboration.team.domain.type.Role;
import com.example.meezy.bc.collaboration.team.domain.vo.InviteCode;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamId;
import com.example.meezy.bc.collaboration.team.domain.vo.TeamMemberId;
import com.example.meezy.bc.user.domain.vo.UserId;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_team")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Team extends AbstractAggregateRoot {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "team_id"))
    private TeamId teamId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id"))
    private UserId leaderId;

    @Column(nullable = false, columnDefinition = "VARCHAR(20)") //팀 이름은 20자 이하여야 한다. -> 도메인 규칙이지만, 지금은 간단히 표현
    private String name;

    @Column(nullable = false)
    private String serverImageUrl;

    @Embedded
    private InviteCode inviteCode;

    @Builder.Default
    @OneToMany(
            mappedBy = "team",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TeamMember> members = new ArrayList<>();

    public static Team create(String name, String serverImageUrl, UserId leaderId) {
        Team team = Team.builder()
                .teamId(TeamId.newId())
                .leaderId(leaderId)
                .name(name)
                .serverImageUrl(serverImageUrl)
                .build();

        // 리더를 첫 번째 멤버로 추가
        team.addLeaderAsMember(leaderId);
        return team;
    }

    public void validateCanDelete(UserId userId){
        validateLeaderPermission(userId);
    }

    public void removeMember(UserId requesterId, TeamMemberId memberId) {
        validateLeaderPermission(requesterId);

        TeamMember member = findMemberById(memberId);
        validateNotRemovingLeader(member);

        members.remove(member);
    }

    public InviteCode generateInviteCode(UserId requesterId) {
        validateLeaderPermission(requesterId);
        InviteCode inViteCode = InviteCode.generate();
        this.inviteCode = inViteCode;
        return inViteCode;
    }

    public void joinByInviteCode(String inviteCode, UserId userId) {
        validateInviteCode(inviteCode);
        validateNotDuplicateMember(userId);

        members.add(
                TeamMember.create(userId, Role.MEMBER, this)
        );
    }

    public void changeName(UserId requesterId, String newName) {
        validateLeaderPermission(requesterId);
        this.name = newName;
    }

    public void changeServerImage(UserId requesterId, String newServerImageUrl) {
        validateLeaderPermission(requesterId);
        this.serverImageUrl = newServerImageUrl;
    }

    public List<TeamMember> getMembers() {
        return Collections.unmodifiableList(members);
    }


    private void addLeaderAsMember(UserId leaderId) {
        TeamMember leaderMember = TeamMember.create(leaderId, Role.LEADER, this);
        members.add(leaderMember);
    }

    public void validateLeaderPermission(UserId userId) {
        if (!isLeader(userId)) {
            throw new NotTeamLeaderException();
        }
    }

    private boolean isLeader(UserId userId) {
        return this.leaderId.equals(userId);
    }

    private void validateNotDuplicateMember(UserId userId) {
        if (isMemberExists(userId)) {
            throw new DuplicateTeamMemberException();
        }
    }

    private boolean isMemberExists(UserId userId) {
        return members.stream()
                .anyMatch(member -> member.getUserId().equals(userId));
    }


    private TeamMember findMemberById(TeamMemberId memberId) {
        return members.stream()
                .filter(member -> member.hasId(memberId))
                .findFirst()
                .orElseThrow(TeamMemberNotFoundException::new);
    }

    private void validateNotRemovingLeader(TeamMember member) {
        if (member.isLeader()) {
            throw new CannotRemoveLeaderException();
        }
    }

    private void validateInviteCode(String inviteCode) {
        if (this.inviteCode == null || this.inviteCode.code() == null) {
            throw new InviteCodeNotGeneratedException();
        }

        if (this.inviteCode.isExpired()) {
            throw new InviteCodeExpiredException();
        }

        if (!this.inviteCode.code().equals(inviteCode)) {
            throw new InvalidInviteCodeException();
        }
    }
}