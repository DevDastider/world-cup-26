package org.sgd.worldcup.mapper;

import org.springframework.stereotype.Component;
import org.sgd.worldcup.dto.MatchStatisticDTO;
import org.sgd.worldcup.entity.MatchStatistic;

@Component
public class MatchStatisticMapper {
    public MatchStatisticDTO toDTO(MatchStatistic statistic) {
        if (statistic == null) {
            return null;
        }
        return MatchStatisticDTO.builder()
                .id(statistic.getId())
                .matchId(statistic.getMatch() != null ? statistic.getMatch().getId() : null)
                .homeTeamId(statistic.getHomeTeam() != null ? statistic.getHomeTeam().getId() : null)
                .awayTeamId(statistic.getAwayTeam() != null ? statistic.getAwayTeam().getId() : null)
                .homeTeamPossession(statistic.getHomeTeamPossession())
                .awayTeamPossession(statistic.getAwayTeamPossession())
                .homeTeamPasses(statistic.getHomeTeamPasses())
                .awayTeamPasses(statistic.getAwayTeamPasses())
                .homeTeamPassAccuracy(statistic.getHomeTeamPassAccuracy())
                .awayTeamPassAccuracy(statistic.getAwayTeamPassAccuracy())
                .createdAt(statistic.getCreatedAt())
                .updatedAt(statistic.getUpdatedAt())
                .build();
    }

    public MatchStatistic toEntity(MatchStatisticDTO dto) {
        if (dto == null) {
            return null;
        }
        return MatchStatistic.builder()
                .id(dto.getId())
                .homeTeamPossession(dto.getHomeTeamPossession())
                .awayTeamPossession(dto.getAwayTeamPossession())
                .homeTeamPasses(dto.getHomeTeamPasses())
                .awayTeamPasses(dto.getAwayTeamPasses())
                .homeTeamPassAccuracy(dto.getHomeTeamPassAccuracy())
                .awayTeamPassAccuracy(dto.getAwayTeamPassAccuracy())
                .build();
    }
}

