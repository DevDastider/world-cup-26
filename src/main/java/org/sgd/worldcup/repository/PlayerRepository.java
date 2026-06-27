package org.sgd.worldcup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sgd.worldcup.entity.Player;
import org.sgd.worldcup.enums.PlayerPosition;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByTeamId(Long teamId);

    Optional<Player> findByTeamIdAndJerseyNumber(Long teamId, Integer jerseyNumber);

    List<Player> findByPositionAndTeamId(PlayerPosition position, Long teamId);

    List<Player> findByPosition(PlayerPosition position);

    @Query("SELECT p FROM Player p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Player> searchByName(@Param("name") String name);

    @Query("SELECT p FROM Player p WHERE p.team.id = :teamId ORDER BY p.tournamentGoals DESC")
    List<Player> findTopScorersByTeam(@Param("teamId") Long teamId);

    boolean existsByTeamIdAndJerseyNumber(Long teamId, Integer jerseyNumber);

    int countByTeamId(Long teamId);

    @Query("SELECT p FROM Player p WHERE p.tournamentGoals > 0 ORDER BY p.tournamentGoals DESC, p.name ASC")
    List<Player> findTopScorers();
}

