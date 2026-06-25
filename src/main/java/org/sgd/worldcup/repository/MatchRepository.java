package org.sgd.worldcup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sgd.worldcup.entity.Match;
import org.sgd.worldcup.enums.MatchStatus;
import org.sgd.worldcup.enums.MatchType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByStatus(MatchStatus status);

    List<Match> findByMatchType(MatchType matchType);

    List<Match> findByGroupId(Long groupId);

    @Query("SELECT m FROM Match m WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId) ORDER BY m.matchDate DESC")
    List<Match> findMatchesByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT m FROM Match m WHERE m.status = :status ORDER BY m.matchDate ASC")
    List<Match> findUpcomingMatches(@Param("status") MatchStatus status);

    @Query("SELECT m FROM Match m WHERE m.matchDate BETWEEN :startDate AND :endDate ORDER BY m.matchDate ASC")
    List<Match> findMatchesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM Match m WHERE m.homeTeam.id = :homeTeamId AND m.awayTeam.id = :awayTeamId ORDER BY m.matchDate DESC")
    List<Match> findMatchesBetweenTeams(@Param("homeTeamId") Long homeTeamId, @Param("awayTeamId") Long awayTeamId);

    List<Match> findByStatusAndGroupId(MatchStatus status, Long groupId);

    @Query("SELECT m FROM Match m WHERE m.status = :status ORDER BY m.matchDate ASC")
    List<Match> findCompletedMatches(@Param("status") MatchStatus status);

    @Query("SELECT COUNT(m) FROM Match m WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId)")
    int countMatchesByTeamId(@Param("teamId") Long teamId);
}

