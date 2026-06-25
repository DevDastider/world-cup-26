package org.sgd.worldcup.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "match_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchStatistic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Match is required")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @NotNull(message = "Home team is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @NotNull(message = "Away team is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

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

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

