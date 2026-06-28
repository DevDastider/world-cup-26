package org.sgd.worldcup.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResolveSlotRequest {

    @NotNull(message = "placeholder team id is required")
    private Long placeholderTeamId;

    @NotNull(message = "realTeamId is required")
    private Long realTeamId;

    @Builder.Default
    private Boolean deletePlaceholder = true;
}
