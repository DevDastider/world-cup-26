package org.sgd.worldcup.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
public class MatchStatisticDTO {
    private Long id;

    @NotNull(message = "Match ID is required")
    private Long matchId;

    @NotNull(message = "Home team ID is required")
    private Long homeTeamId;

    @NotNull(message = "Away team ID is required")
    private Long awayTeamId;

    @DecimalMin(value = "0.0", inclusive = true, message = "Possession must be between 0 and 100")
    @DecimalMax(value = "100.0", inclusive = true, message = "Possession must be between 0 and 100")
    private Double homeTeamPossession;

    @DecimalMin(value = "0.0", inclusive = true, message = "Possession must be between 0 and 100")
    @DecimalMax(value = "100.0", inclusive = true, message = "Possession must be between 0 and 100")
    private Double awayTeamPossession;

    @Min(value = 0, message = "Passes cannot be negative")
    private Integer homeTeamPasses;

    @Min(value = 0, message = "Passes cannot be negative")
    private Integer awayTeamPasses;

    @DecimalMin(value = "0.0", inclusive = true, message = "Pass accuracy must be between 0 and 100")
    @DecimalMax(value = "100.0", inclusive = true, message = "Pass accuracy must be between 0 and 100")
    private Double homeTeamPassAccuracy;

    @DecimalMin(value = "0.0", inclusive = true, message = "Pass accuracy must be between 0 and 100")
    @DecimalMax(value = "100.0", inclusive = true, message = "Pass accuracy must be between 0 and 100")
    private Double awayTeamPassAccuracy;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

