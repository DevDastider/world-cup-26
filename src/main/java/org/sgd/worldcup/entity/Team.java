package org.sgd.worldcup.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "teams", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name"),
        @UniqueConstraint(columnNames = "country_code")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 3, message = "Country code must be between 2 and 3 characters")
    @Column(nullable = false, unique = true, length = 3)
    private String countryCode;

    @Size(max = 50, message = "Confederation cannot exceed 50 characters")
    private String confederation;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Player> players;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupTeam> groupTeams;

    @OneToMany(mappedBy = "homeTeam")
    private Set<Match> homeMatches;

    @OneToMany(mappedBy = "awayTeam")
    private Set<Match> awayMatches;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

