package org.sgd.worldcup.dto;

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

    @Min(value = 100, message = "Height must be at least 100 cm")
    @Max(value = 250, message = "Height cannot exceed 250 cm")
    private Integer height;

    @Min(value = 30, message = "Weight must be at least 30 kg")
    @Max(value = 150, message = "Weight cannot exceed 150 kg")
    private Integer weight;

    @Min(value = 0, message = "International caps cannot be negative")
    private Integer internationalCaps;

    @Min(value = 0, message = "Goals in career cannot be negative")
    private Integer goalsInCareer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

