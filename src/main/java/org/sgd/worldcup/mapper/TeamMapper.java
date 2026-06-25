package org.sgd.worldcup.mapper;

import org.springframework.stereotype.Component;
import org.sgd.worldcup.dto.TeamDTO;
import org.sgd.worldcup.entity.Team;

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
                .build();
    }
}

