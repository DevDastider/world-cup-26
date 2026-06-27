package org.sgd.worldcup.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sgd.worldcup.enums.PlayerPosition;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDTO {
    private Long id;

    @NotNull(message = "Team ID is required")
    private Long teamId;

    private TeamDTO team;

    @NotBlank(message = "Player name is required")
    @Size(min = 2, max = 100, message = "Player name must be between 2 and 100 characters")
    private String name;

    @NotNull(message = "Jersey number is required")
    @Min(value = 1, message = "Jersey number must be at least 1")
    @Max(value = 99, message = "Jersey number cannot exceed 99")
    private Integer jerseyNumber;

    private PlayerPosition position;

    @PastOrPresent(message = "Date of birth cannot be in the future")
    private LocalDate dateOfBirth;

    @Size(max = 150, message = "Club name cannot exceed 150 characters")
    private String clubName;

    @Size(max = 3, message = "Club country code cannot exceed 3 characters")
    private String clubCountry;

    @Min(value=0, message = "Tournament goals cannot be negative.")
    private Integer tournamentGoals;

    @Min(value=0, message = "Own goals cannot be negative.")
    private Integer ownGoals;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

