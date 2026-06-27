package org.sgd.worldcup.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
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
@Getter
@Setter
@ToString
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
    @ToString.Exclude
    private Team homeTeam;

    @NotNull(message = "Away team is required")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "away_team_id", nullable = false)
    @ToString.Exclude
    private Team awayTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @ToString.Exclude
    private Group group;

    @NotNull(message = "Match type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MatchType matchType;

    @NotNull(message = "Match date is required")
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

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Goal> goals;

    @OneToOne(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
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
        Match other = (Match) obj;
        //equal only when ids match
        return id != null && id.equals(other.id);
    }
}

