package org.sgd.worldcup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamDTO {
    private Long id;

    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 100, message = "Team name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 3, message = "Country code must be between 2 and 3 characters")
    private String countryCode;

    @Size(max = 50, message = "Confederation cannot exceed 50 characters")
    private String confederation;

    @Size(max = 100, message = "Normalised name cannot exceed 100 characters")
    private String nameNormalised;

    @Size(max = 50, message = "Continent cannot exceed 50 characters")
    private String continent;

    @Size(max = 16, message = "Flag icon cannot exceed 16 characters")
    private String flagIcon;

    private boolean placeholder;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

