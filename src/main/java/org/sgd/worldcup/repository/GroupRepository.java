package org.sgd.worldcup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.sgd.worldcup.entity.Group;
import org.sgd.worldcup.enums.StageType;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String name);

//    List<Group> findByStage(StageType stage);

    boolean existsByName(String name);
}

