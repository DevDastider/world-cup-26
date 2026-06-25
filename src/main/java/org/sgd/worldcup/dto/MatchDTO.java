package org.sgd.worldcup.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sgd.worldcup.enums.MatchStatus;
import org.sgd.worldcup.enums.MatchType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchDTO {
    private Long id;

    @NotNull(message = "Home team ID is required")
    private Long homeTeamId;

    private TeamDTO homeTeam;

    @NotNull(message = "Away team ID is required")
    private Long awayTeamId;

    private TeamDTO awayTeam;

    private Long groupId;

    @NotNull(message = "Match type is required")
    private MatchType matchType;

    @NotNull(message = "Match date is required")
    @FutureOrPresent(message = "Match date cannot be in the past")
    private LocalDateTime matchDate;

    @Size(max = 200, message = "Venue cannot exceed 200 characters")
    private String venue;

    @Min(value = 0, message = "Home team goals cannot be negative")
    private Integer homeTeamGoals;

    @Min(value = 0, message = "Away team goals cannot be negative")
    private Integer awayTeamGoals;

    @NotNull(message = "Match status is required")
    private MatchStatus status;

    // Match Statistics
    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double homeTeamPossessionPercentage;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0", inclusive = true)
    private Double awayTeamPossessionPercentage;

    @Min(value = 0)
    private Integer homeTeamShots;

    @Min(value = 0)
    private Integer awayTeamShots;

    @Min(value = 0)
    private Integer homeTeamShotsOnTarget;

    @Min(value = 0)
    private Integer awayTeamShotsOnTarget;

    @Min(value = 0)
    private Integer homeTeamFouls;

    @Min(value = 0)
    private Integer awayTeamFouls;

    @Min(value = 0)
    @Max(value = 11)
    private Integer homeTeamYellowCards;

    @Min(value = 0)
    @Max(value = 11)
    private Integer awayTeamYellowCards;

    @Min(value = 0)
    @Max(value = 11)
    private Integer homeTeamRedCards;

    @Min(value = 0)
    @Max(value = 11)
    private Integer awayTeamRedCards;

    @Min(value = 0)
    private Integer homeTeamCorners;

    @Min(value = 0)
    private Integer awayTeamCorners;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

