package org.sgd.worldcup.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.sgd.worldcup.enums.PlayerPosition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "players", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"team_id", "jersey_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Team is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @NotBlank(message = "Player name is required")
    @Size(min = 2, max = 100, message = "Player name must be between 2 and 100 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Jersey number is required")
    @Min(value = 1, message = "Jersey number must be at least 1")
    @Max(value = 99, message = "Jersey number cannot exceed 99")
    @Column(nullable = false)
    private Integer jerseyNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PlayerPosition position;

    @PastOrPresent(message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;

    @Min(value = 100, message = "Height must be at least 100 cm")
    @Max(value = 250, message = "Height cannot exceed 250 cm")
    private Integer height;

    @Min(value = 30, message = "Weight must be at least 30 kg")
    @Max(value = 150, message = "Weight cannot exceed 150 kg")
    private Integer weight;

    @Min(value = 0, message = "International caps cannot be negative")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer internationalCaps = 0;

    @Min(value = 0, message = "Goals in career cannot be negative")
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer goalsInCareer = 0;

    @OneToMany(mappedBy = "player")
    private Set<Goal> goals;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

