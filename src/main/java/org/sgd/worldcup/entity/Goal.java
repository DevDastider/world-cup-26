package org.sgd.worldcup.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
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
@Getter
@Setter
@ToString
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
    @ToString.Exclude
    private Match match;

    @NotNull(message = "Player is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "player_id", nullable = false)
    @ToString.Exclude
    private Player player;

    @NotNull(message = "Scoring team is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scoring_team_id", nullable = false)
    @ToString.Exclude
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
        //Own goals are scored by an opposing player into their own net, so the
        // scorer intentionally does not belong to the scoring(beneficiary) team.
        boolean ownGoal = goalType == GoalType.OWN_GOAL || goalType == GoalType.PENALTY_OWN_GOAL;

        //For normal goals, the scorer must belong to the scoring team.
        if (!ownGoal && player != null && scoringTeam != null && !player.getTeam().getId().equals(scoringTeam.getId())) {
            throw new IllegalArgumentException("Player must belong to the scoring team");
        }

        //For own goals, the scorer must NOT belong to the beneficiary team.
        if(ownGoal && player != null && scoringTeam != null && player.getTeam().getId().equals(scoringTeam.getId())) {
            throw new IllegalArgumentException("Own goal scorer cannot belong to the beneficiary team");
        }
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // Compare entities by identity
        //Same reference, return equal
        if (this == obj) return true;
        //Null, return false
        if (obj == null) return false;
        //unwrap proxy+type check
        if (Hibernate.getClass(this) != Hibernate.getClass(obj)) return false;
        Goal other = (Goal) obj;
        //equal only when ids match
        return id != null && id.equals(other.id);
    }
}

