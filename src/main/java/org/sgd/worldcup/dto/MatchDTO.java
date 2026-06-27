package org.sgd.worldcup.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    private LocalDateTime matchDate;

    @Size(max = 200, message = "Venue cannot exceed 200 characters")
    private String venue;

    @Min(value = 0, message = "Home team goals cannot be negative")
    private Integer homeTeamGoals;

    @Min(value = 0, message = "Away team goals cannot be negative")
    private Integer awayTeamGoals;

    @NotNull(message = "Match status is required")
    private MatchStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

