package org.sgd.worldcup.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sgd.worldcup.dto.MatchDTO;
import org.sgd.worldcup.entity.Match;

@Component
public class MatchMapper {
    @Autowired
    private TeamMapper teamMapper;

    public MatchDTO toDTO(Match match) {
        if (match == null) {
            return null;
        }
        return MatchDTO.builder()
                .id(match.getId())
                .homeTeamId(match.getHomeTeam() != null ? match.getHomeTeam().getId() : null)
                .homeTeam(match.getHomeTeam() != null ? teamMapper.toDTO(match.getHomeTeam()) : null)
                .awayTeamId(match.getAwayTeam() != null ? match.getAwayTeam().getId() : null)
                .awayTeam(match.getAwayTeam() != null ? teamMapper.toDTO(match.getAwayTeam()) : null)
                .groupId(match.getGroup() != null ? match.getGroup().getId() : null)
                .matchType(match.getMatchType())
                .matchDate(match.getMatchDate())
                .venue(match.getVenue())
                .homeTeamGoals(match.getHomeTeamGoals())
                .awayTeamGoals(match.getAwayTeamGoals())
                .status(match.getStatus())
                .homeTeamPossessionPercentage(match.getHomeTeamPossessionPercentage())
                .awayTeamPossessionPercentage(match.getAwayTeamPossessionPercentage())
                .homeTeamShots(match.getHomeTeamShots())
                .awayTeamShots(match.getAwayTeamShots())
                .homeTeamShotsOnTarget(match.getHomeTeamShotsOnTarget())
                .awayTeamShotsOnTarget(match.getAwayTeamShotsOnTarget())
                .homeTeamFouls(match.getHomeTeamFouls())
                .awayTeamFouls(match.getAwayTeamFouls())
                .homeTeamYellowCards(match.getHomeTeamYellowCards())
                .awayTeamYellowCards(match.getAwayTeamYellowCards())
                .homeTeamRedCards(match.getHomeTeamRedCards())
                .awayTeamRedCards(match.getAwayTeamRedCards())
                .homeTeamCorners(match.getHomeTeamCorners())
                .awayTeamCorners(match.getAwayTeamCorners())
                .createdAt(match.getCreatedAt())
                .updatedAt(match.getUpdatedAt())
                .build();
    }

    public Match toEntity(MatchDTO matchDTO) {
        if (matchDTO == null) {
            return null;
        }
        return Match.builder()
                .id(matchDTO.getId())
                .matchType(matchDTO.getMatchType())
                .matchDate(matchDTO.getMatchDate())
                .venue(matchDTO.getVenue())
                .homeTeamGoals(matchDTO.getHomeTeamGoals())
                .awayTeamGoals(matchDTO.getAwayTeamGoals())
                .status(matchDTO.getStatus())
                .homeTeamPossessionPercentage(matchDTO.getHomeTeamPossessionPercentage())
                .awayTeamPossessionPercentage(matchDTO.getAwayTeamPossessionPercentage())
                .homeTeamShots(matchDTO.getHomeTeamShots())
                .awayTeamShots(matchDTO.getAwayTeamShots())
                .homeTeamShotsOnTarget(matchDTO.getHomeTeamShotsOnTarget())
                .awayTeamShotsOnTarget(matchDTO.getAwayTeamShotsOnTarget())
                .homeTeamFouls(matchDTO.getHomeTeamFouls())
                .awayTeamFouls(matchDTO.getAwayTeamFouls())
                .homeTeamYellowCards(matchDTO.getHomeTeamYellowCards())
                .awayTeamYellowCards(matchDTO.getAwayTeamYellowCards())
                .homeTeamRedCards(matchDTO.getHomeTeamRedCards())
                .awayTeamRedCards(matchDTO.getAwayTeamRedCards())
                .homeTeamCorners(matchDTO.getHomeTeamCorners())
                .awayTeamCorners(matchDTO.getAwayTeamCorners())
                .build();
    }
}

