package org.sgd.worldcup.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTeamDTO {
    private Long id;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Team is required")
    private TeamDTO team;

    @Min(value = 0, message = "Group position must be non-negative")
    private Integer groupPosition;

    @Min(value = 0, message = "Wins cannot be negative")
    private Integer wins;

    @Min(value = 0, message = "Losses cannot be negative")
    private Integer losses;

    @Min(value = 0, message = "Draws cannot be negative")
    private Integer draws;

    @Min(value = 0, message = "Goals for cannot be negative")
    private Integer goalsFor;

    @Min(value = 0, message = "Goals against cannot be negative")
    private Integer goalsAgainst;

    @Min(value = 0, message = "Points cannot be negative")
    private Integer points;

    private Integer goalDifference;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

