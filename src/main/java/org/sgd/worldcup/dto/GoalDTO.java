package org.sgd.worldcup.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sgd.worldcup.enums.GoalType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalDTO {
    private Long id;

    @NotNull(message = "Match ID is required")
    private Long matchId;

    @NotNull(message = "Player ID is required")
    private Long playerId;

    private PlayerDTO player;

    @NotNull(message = "Scoring team ID is required")
    private Long scoringTeamId;

    private TeamDTO scoringTeam;

    @NotNull(message = "Minute is required")
    @Min(value = 1, message = "Minute must be at least 1")
    @Max(value = 150, message = "Minute cannot exceed 150")
    private Integer minute;

    @NotNull(message = "Goal type is required")
    private GoalType goalType;

    private Boolean isPenaltyGoal;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

