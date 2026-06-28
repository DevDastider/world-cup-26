package org.sgd.worldcup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchTeamUpdateRequest {

    private Long homeTeamId;

    private Long awayTeamId;
}
