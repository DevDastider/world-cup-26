package org.sgd.worldcup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sgd.worldcup.entity.Goal;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {
    List<Goal> findByMatchId(Long matchId);

    List<Goal> findByPlayerId(Long playerId);

    List<Goal> findByScoringTeamId(Long teamId);

    @Query("SELECT g FROM Goal g WHERE g.match.id = :matchId ORDER BY g.minute ASC")
    List<Goal> findGoalsByMatchIdOrderByMinute(@Param("matchId") Long matchId);

    @Query("SELECT g FROM Goal g WHERE g.player.id = :playerId ORDER BY g.createdAt DESC")
    List<Goal> findGoalsByPlayerIdOrderByDate(@Param("playerId") Long playerId);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.player.id = :playerId")
    int countGoalsByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT COUNT(g) FROM Goal g WHERE g.scoringTeam.id = :teamId AND g.match.id = :matchId")
    int countGoalsByTeamInMatch(@Param("teamId") Long teamId, @Param("matchId") Long matchId);

    @Query("SELECT g FROM Goal g WHERE g.match.homeTeam.id = :teamId OR g.match.awayTeam.id = :teamId ORDER BY g.match.matchDate DESC")
    List<Goal> findGoalsByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT g.player, COUNT(g) as goalCount FROM Goal g GROUP BY g.player ORDER BY goalCount DESC")
    List<Object[]> findTopScorers();
}

