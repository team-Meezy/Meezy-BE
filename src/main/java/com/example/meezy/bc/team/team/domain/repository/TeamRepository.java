package com.example.meezy.bc.team.team.domain.repository;

import com.example.meezy.bc.team.team.domain.Team;
import com.example.meezy.bc.team.team.domain.vo.TeamId;
import com.example.meezy.bc.user.domain.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, TeamId> {
    Optional<Team> findByTeamId_Value(UUID teamIdValue);

    @Query("SELECT DISTINCT t FROM Team t " +
            "LEFT JOIN FETCH t.members m " +
            "WHERE m.userId = :userId")
    List<Team> findTeamsByUserIdWithMembers(@Param("userId") UserId userId);

    Optional<Team> findByInviteCode_Code(String code);
}
