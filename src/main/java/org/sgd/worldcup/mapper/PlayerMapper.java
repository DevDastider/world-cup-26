package org.sgd.worldcup.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sgd.worldcup.dto.PlayerDTO;
import org.sgd.worldcup.entity.Player;

@Component
public class PlayerMapper {
    @Autowired
    private TeamMapper teamMapper;

    public PlayerDTO toDTO(Player player) {
        if (player == null) {
            return null;
        }
        return PlayerDTO.builder()
                .id(player.getId())
                .teamId(player.getTeam() != null ? player.getTeam().getId() : null)
                .team(player.getTeam() != null ? teamMapper.toDTO(player.getTeam()) : null)
                .name(player.getName())
                .jerseyNumber(player.getJerseyNumber())
                .position(player.getPosition())
                .dateOfBirth(player.getDateOfBirth())
                .height(player.getHeight())
                .weight(player.getWeight())
                .internationalCaps(player.getInternationalCaps())
                .goalsInCareer(player.getGoalsInCareer())
                .createdAt(player.getCreatedAt())
                .updatedAt(player.getUpdatedAt())
                .build();
    }

    public Player toEntity(PlayerDTO playerDTO) {
        if (playerDTO == null) {
            return null;
        }
        return Player.builder()
                .id(playerDTO.getId())
                .name(playerDTO.getName())
                .jerseyNumber(playerDTO.getJerseyNumber())
                .position(playerDTO.getPosition())
                .dateOfBirth(playerDTO.getDateOfBirth())
                .height(playerDTO.getHeight())
                .weight(playerDTO.getWeight())
                .internationalCaps(playerDTO.getInternationalCaps())
                .goalsInCareer(playerDTO.getGoalsInCareer())
                .build();
    }
}

