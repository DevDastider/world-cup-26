package org.sgd.worldcup.mapper;

import org.sgd.worldcup.dto.TeamDTO;
import org.sgd.worldcup.entity.Team;
import org.springframework.stereotype.Component;

@Component
public class TeamMapper {
    public TeamDTO toDTO(Team team) {
        if (team == null) {
            return null;
        }
        return TeamDTO.builder()
                .id(team.getId())
                .name(team.getName())
                .countryCode(team.getCountryCode())
                .confederation(team.getConfederation())
                .nameNormalised(team.getNameNormalised())
                .continent(team.getContinent())
                .flagIcon(team.getFlagIcon())
                .placeholder(team.isPlaceholder())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    public Team toEntity(TeamDTO teamDTO) {
        if (teamDTO == null) {
            return null;
        }
        return Team.builder()
                .id(teamDTO.getId())
                .name(teamDTO.getName())
                .countryCode(teamDTO.getCountryCode())
                .confederation(teamDTO.getConfederation())
                .nameNormalised(teamDTO.getNameNormalised())
                .continent(teamDTO.getContinent())
                .flagIcon(teamDTO.getFlagIcon())
                .placeholder(teamDTO.isPlaceholder())
                .build();
    }
}

