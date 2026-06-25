package org.sgd.worldcup.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.sgd.worldcup.enums.MatchStatus;
import org.sgd.worldcup.enums.MatchType;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "matches", indexes = {
        @Index(name = "idx_matches_home_team", columnList = "home_team_id"),
        @Index(name = "idx_matches_away_team", columnList = "away_team_id"),
        @Index(name = "idx_matches_match_date", columnList = "match_date"),
        @Index(name = "idx_matches_status", columnList = "status"),
        @Index(name = "idx_matches_group", columnList = "group_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Home team is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @NotNull(message = "Away team is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @NotNull(message = "Match type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MatchType matchType;

    @NotNull(message = "Match date is required")
    @FutureOrPresent(message = "Match date cannot be in the past")
    @Column(nullable = false)
    private LocalDateTime matchDate;

    @Size(max = 200, message = "Venue cannot exceed 200 characters")
    private String venue;

    @Min(value = 0, message = "Home team goals cannot be negative")
    private Integer homeTeamGoals;

    @Min(value = 0, message = "Away team goals cannot be negative")
    private Integer awayTeamGoals;

    @NotNull(message = "Match status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MatchStatus status = MatchStatus.SCHEDULED;

    // Match Statistics
    @DecimalMin(value = "0.0", inclusive = true, message = "Possession percentage must be between 0 and 100")
    @DecimalMax(value = "100.0", inclusive = true, message = "Possession percentage must be between 0 and 100")
    private Double homeTeamPossessionPercentage;

    @DecimalMin(value = "0.0", inclusive = true, message = "Possession percentage must be between 0 and 100")
    @DecimalMax(value = "100.0", inclusive = true, message = "Possession percentage must be between 0 and 100")
    private Double awayTeamPossessionPercentage;

    @Min(value = 0, message = "Shots cannot be negative")
    private Integer homeTeamShots;

    @Min(value = 0, message = "Shots cannot be negative")
    private Integer awayTeamShots;

    @Min(value = 0, message = "Shots on target cannot be negative")
    private Integer homeTeamShotsOnTarget;

    @Min(value = 0, message = "Shots on target cannot be negative")
    private Integer awayTeamShotsOnTarget;

    @Min(value = 0, message = "Fouls cannot be negative")
    private Integer homeTeamFouls;

    @Min(value = 0, message = "Fouls cannot be negative")
    private Integer awayTeamFouls;

    @Min(value = 0, message = "Yellow cards cannot be negative")
    @Max(value = 11, message = "Yellow cards cannot exceed 11")
    private Integer homeTeamYellowCards;

    @Min(value = 0, message = "Yellow cards cannot be negative")
    @Max(value = 11, message = "Yellow cards cannot exceed 11")
    private Integer awayTeamYellowCards;

    @Min(value = 0, message = "Red cards cannot be negative")
    @Max(value = 11, message = "Red cards cannot exceed 11")
    private Integer homeTeamRedCards;

    @Min(value = 0, message = "Red cards cannot be negative")
    @Max(value = 11, message = "Red cards cannot exceed 11")
    private Integer awayTeamRedCards;

    @Min(value = 0, message = "Corners cannot be negative")
    private Integer homeTeamCorners;

    @Min(value = 0, message = "Corners cannot be negative")
    private Integer awayTeamCorners;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Goal> goals;

    @OneToOne(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private MatchStatistic matchStatistic;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void validateTeams() {
        if (homeTeam != null && awayTeam != null && homeTeam.getId().equals(awayTeam.getId())) {
            throw new IllegalArgumentException("Home team and away team cannot be the same");
        }
    }
}

