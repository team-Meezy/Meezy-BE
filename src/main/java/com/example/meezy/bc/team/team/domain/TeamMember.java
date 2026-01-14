package com.example.meezy.bc.team.team.domain;

import com.example.meezy.bc.team.team.domain.type.Role;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import com.example.meezy.bc.team.team.domain.vo.TeamMemberId;
import com.example.meezy.bc.user.domain.vo.UserId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "tbl_team_member")
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember {

    @EmbeddedId
    @AttributeOverride(name = "value", column = @Column(name = "team_member_id"))
    private TeamMemberId teamMemberId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "user_id"))
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    public static TeamMember create(UserId userId, Role role, Team team){
        return TeamMember.builder()
                .teamMemberId(TeamMemberId.newId())
                .userId(userId)
                .role(role != null ? role : Role.MEMBER)
                .team(team)
                .build();
    }

    public boolean isLeader() {
        return this.role == Role.LEADER;
    }

    boolean hasId(TeamMemberId memberId) {
        return this.teamMemberId.equals(memberId);
    }
}
