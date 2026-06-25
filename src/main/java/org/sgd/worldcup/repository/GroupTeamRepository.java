package org.sgd.worldcup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.sgd.worldcup.entity.GroupTeam;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupTeamRepository extends JpaRepository<GroupTeam, Long> {
    Optional<GroupTeam> findByGroupIdAndTeamId(Long groupId, Long teamId);

    List<GroupTeam> findByGroupId(Long groupId);

    List<GroupTeam> findByTeamId(Long teamId);

    @Query("SELECT gt FROM GroupTeam gt WHERE gt.group.id = :groupId ORDER BY gt.points DESC, gt.goalDifference DESC, gt.goalsFor DESC")
    List<GroupTeam> findGroupStandingsByGroupId(@Param("groupId") Long groupId);

    void deleteByGroupIdAndTeamId(Long groupId, Long teamId);

    boolean existsByGroupIdAndTeamId(Long groupId, Long teamId);

    int countByGroupId(Long groupId);
}

