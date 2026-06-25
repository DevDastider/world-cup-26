package org.sgd.worldcup.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_teams", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "team_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTeam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Group is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @NotNull(message = "Team is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Min(value = 0, message = "Group position must be non-negative")
    private Integer groupPosition;

    @Min(value = 0, message = "Wins cannot be negative")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer wins = 0;

    @Min(value = 0, message = "Losses cannot be negative")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer losses = 0;

    @Min(value = 0, message = "Draws cannot be negative")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer draws = 0;

    @Min(value = 0, message = "Goals for cannot be negative")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer goalsFor = 0;

    @Min(value = 0, message = "Goals against cannot be negative")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer goalsAgainst = 0;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer goalDifference = 0;

    @Min(value = 0, message = "Points cannot be negative")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer points = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PreUpdate
    public void updateGoalDifference() {
        this.goalDifference = this.goalsFor - this.goalsAgainst;
    }

    @PostLoad
    public void calculateGoalDifference() {
        this.goalDifference = this.goalsFor - this.goalsAgainst;
    }
}

