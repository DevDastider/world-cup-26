package org.sgd.worldcup.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
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

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "teams", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name"),
        @UniqueConstraint(columnNames = "country_code")
})
@Getter
@Setter
@ToString
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

    @Size(max = 100, message = "Normalised name cannot exceed 100 characters")
    @Column(name= "name_normalised", length = 100)
    private String nameNormalised;

    @Size(max = 50, message = "Continent cannot exceed 50 characters")
    @Column(length = 50)
    private String continent;

    @Size(max = 16, message = "Flag icon cannot exceed 16 characters")
    @Column(name = "flag_icon", length = 16)
    private String flagIcon;

    @Column(nullable = false)
    private boolean placeholder = false;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Player> players;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<GroupTeam> groupTeams;

    @OneToMany(mappedBy = "homeTeam")
    @ToString.Exclude
    private Set<Match> homeMatches;

    @OneToMany(mappedBy = "awayTeam")
    @ToString.Exclude
    private Set<Match> awayMatches;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

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
        Team other = (Team) obj;
        //equal only when ids match
        return id != null && id.equals(other.id);
    }
}

