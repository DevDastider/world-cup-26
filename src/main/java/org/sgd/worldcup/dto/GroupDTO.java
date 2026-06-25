package org.sgd.worldcup.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sgd.worldcup.enums.StageType;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDTO {
    private Long id;

    @NotBlank(message = "Group name is required")
    @Size(min = 1, max = 50, message = "Group name must be between 1 and 50 characters")
    private String name;

//    @NotNull(message = "Stage type is required")
//    private StageType stage;

    private Set<GroupTeamDTO> groupTeams;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

