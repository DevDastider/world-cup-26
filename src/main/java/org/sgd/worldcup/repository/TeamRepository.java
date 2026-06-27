package org.sgd.worldcup.repository;

import org.sgd.worldcup.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);

    Optional<Team> findByCountryCode(String countryCode);

    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.countryCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Team> searchTeams(@Param("searchTerm") String searchTerm);

    List<Team> findByConfederation(String confederation);

    List<Team> findByPlaceholderFalse();

    List<Team> findByPlaceholderTrue();

    boolean existsByCountryCode(String countryCode);
}

