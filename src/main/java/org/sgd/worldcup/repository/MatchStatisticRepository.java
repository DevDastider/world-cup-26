package org.sgd.worldcup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sgd.worldcup.entity.MatchStatistic;

import java.util.Optional;

@Repository
public interface MatchStatisticRepository extends JpaRepository<MatchStatistic, Long> {
    Optional<MatchStatistic> findByMatchId(Long matchId);

    void deleteByMatchId(Long matchId);
}

