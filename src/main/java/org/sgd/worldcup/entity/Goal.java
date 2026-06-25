package org.sgd.worldcup.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.sgd.worldcup.enums.GoalType;

import java.time.LocalDateTime;

@Entity
@Table(name = "goals", indexes = {
        @Index(name = "idx_goals_match", columnList = "match_id"),
        @Index(name = "idx_goals_player", columnList = "player_id"),
        @Index(name = "idx_goals_team", columnList = "scoring_team_id"),
        @Index(name = "idx_goals_minute", columnList = "minute")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Match is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @NotNull(message = "Player is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @NotNull(message = "Scoring team is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scoring_team_id", nullable = false)
    private Team scoringTeam;

    @NotNull(message = "Minute is required")
    @Min(value = 1, message = "Minute must be at least 1")
    @Max(value = 150, message = "Minute cannot exceed 150")
    @Column(nullable = false)
    private Integer minute;

    @NotNull(message = "Goal type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private GoalType goalType;

    @Column(nullable = false)
    private Boolean isPenaltyGoal = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    private void validateGoal() {
        // Validate that the player belongs to the scoring team
        if (player != null && scoringTeam != null && !player.getTeam().getId().equals(scoringTeam.getId())) {
            throw new IllegalArgumentException("Player must belong to the scoring team");
        }

        // If it's a penalty goal, it should be marked as PENALTY in goalType
        if (isPenaltyGoal && goalType != GoalType.PENALTY && goalType != GoalType.PENALTY_OWN_GOAL) {
            // Allow to continue but log warning
        }
    }
}

