package org.sgd.worldcup.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sgd.worldcup.dto.GoalDTO;
import org.sgd.worldcup.entity.Goal;

@Component
public class GoalMapper {
    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private TeamMapper teamMapper;

    public GoalDTO toDTO(Goal goal) {
        if (goal == null) {
            return null;
        }
        return GoalDTO.builder()
                .id(goal.getId())
                .matchId(goal.getMatch() != null ? goal.getMatch().getId() : null)
                .playerId(goal.getPlayer() != null ? goal.getPlayer().getId() : null)
                .player(goal.getPlayer() != null ? playerMapper.toDTO(goal.getPlayer()) : null)
                .scoringTeamId(goal.getScoringTeam() != null ? goal.getScoringTeam().getId() : null)
                .scoringTeam(goal.getScoringTeam() != null ? teamMapper.toDTO(goal.getScoringTeam()) : null)
                .minute(goal.getMinute())
                .goalType(goal.getGoalType())
                .isPenaltyGoal(goal.getIsPenaltyGoal())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }

    public Goal toEntity(GoalDTO goalDTO) {
        if (goalDTO == null) {
            return null;
        }
        return Goal.builder()
                .id(goalDTO.getId())
                .minute(goalDTO.getMinute())
                .goalType(goalDTO.getGoalType())
                .isPenaltyGoal(goalDTO.getIsPenaltyGoal() != null ? goalDTO.getIsPenaltyGoal() : false)
                .build();
    }
}

