package org.sgd.worldcup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sgd.worldcup.dto.PlayerDTO;
import org.sgd.worldcup.entity.Player;
import org.sgd.worldcup.entity.Team;
import org.sgd.worldcup.enums.PlayerPosition;
import org.sgd.worldcup.exception.DuplicateResourceException;
import org.sgd.worldcup.exception.ResourceNotFoundException;
import org.sgd.worldcup.mapper.PlayerMapper;
import org.sgd.worldcup.repository.PlayerRepository;
import org.sgd.worldcup.repository.TeamRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerMapper playerMapper;

    public PlayerDTO createPlayer(PlayerDTO playerDTO) {
        log.info("Creating new player: {} for team: {}", playerDTO.getName(), playerDTO.getTeamId());

        Team team = teamRepository.findById(playerDTO.getTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + playerDTO.getTeamId()));

        if (playerRepository.existsByTeamIdAndJerseyNumber(playerDTO.getTeamId(), playerDTO.getJerseyNumber())) {
            throw new DuplicateResourceException("Player with jersey number " + playerDTO.getJerseyNumber() + " already exists in this team");
        }

        Player player = playerMapper.toEntity(playerDTO);
        player.setTeam(team);
        Player savedPlayer = playerRepository.save(player);
        log.info("Player created successfully with ID: {}", savedPlayer.getId());
        return playerMapper.toDTO(savedPlayer);
    }

    public PlayerDTO getPlayerById(Long id) {
        log.info("Fetching player with ID: {}", id);
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + id));
        return playerMapper.toDTO(player);
    }

    public List<PlayerDTO> getAllPlayers() {
        log.info("Fetching all players");
        return playerRepository.findAll().stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getPlayersByTeam(Long teamId) {
        log.info("Fetching players for team: {}", teamId);
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }
        return playerRepository.findByTeamId(teamId).stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getPlayersByPosition(PlayerPosition position) {
        log.info("Fetching players with position: {}", position);
        return playerRepository.findByPosition(position).stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> searchPlayers(String name) {
        log.info("Searching players with name: {}", name);
        return playerRepository.searchByName(name).stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getTopScorers() {
        log.info("Fetching top scorers");
        return playerRepository.findTopScorers().stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<PlayerDTO> getTopScorersByTeam(Long teamId) {
        log.info("Fetching top scorers for team: {}", teamId);
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }
        return playerRepository.findTopScorersByTeam(teamId).stream()
                .map(playerMapper::toDTO)
                .collect(Collectors.toList());
    }

    public PlayerDTO updatePlayer(Long id, PlayerDTO playerDTO) {
        log.info("Updating player with ID: {}", id);
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + id));

        if (!player.getJerseyNumber().equals(playerDTO.getJerseyNumber()) &&
                playerRepository.existsByTeamIdAndJerseyNumber(player.getTeam().getId(), playerDTO.getJerseyNumber())) {
            throw new DuplicateResourceException("Player with jersey number " + playerDTO.getJerseyNumber() + " already exists in this team");
        }

        player.setName(playerDTO.getName());
        player.setJerseyNumber(playerDTO.getJerseyNumber());
        player.setPosition(playerDTO.getPosition());
        player.setDateOfBirth(playerDTO.getDateOfBirth());
        player.setClubName(playerDTO.getClubName());
        player.setClubCountry(playerDTO.getClubCountry());

        Player updatedPlayer = playerRepository.save(player);
        log.info("Player updated successfully with ID: {}", id);
        return playerMapper.toDTO(updatedPlayer);
    }

    public void deletePlayer(Long id) {
        log.info("Deleting player with ID: {}", id);
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + id));
        playerRepository.delete(player);
        log.info("Player deleted successfully with ID: {}", id);
    }

    public boolean existsById(Long id) {
        return playerRepository.existsById(id);
    }
}

