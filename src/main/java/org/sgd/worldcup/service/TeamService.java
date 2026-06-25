package org.sgd.worldcup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sgd.worldcup.dto.TeamDTO;
import org.sgd.worldcup.entity.Team;
import org.sgd.worldcup.exception.DuplicateResourceException;
import org.sgd.worldcup.exception.ResourceNotFoundException;
import org.sgd.worldcup.mapper.TeamMapper;
import org.sgd.worldcup.repository.TeamRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMapper teamMapper;

    public TeamDTO createTeam(TeamDTO teamDTO) {
        log.info("Creating new team: {}", teamDTO.getName());

        if (teamRepository.existsByCountryCode(teamDTO.getCountryCode())) {
            throw new DuplicateResourceException("Team with country code '" + teamDTO.getCountryCode() + "' already exists");
        }

        Team team = teamMapper.toEntity(teamDTO);
        Team savedTeam = teamRepository.save(team);
        log.info("Team created successfully with ID: {}", savedTeam.getId());
        return teamMapper.toDTO(savedTeam);
    }

    public TeamDTO getTeamById(Long id) {
        log.info("Fetching team with ID: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));
        return teamMapper.toDTO(team);
    }

    public TeamDTO getTeamByName(String name) {
        log.info("Fetching team with name: {}", name);
        Team team = teamRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with name: " + name));
        return teamMapper.toDTO(team);
    }

    public TeamDTO getTeamByCountryCode(String countryCode) {
        log.info("Fetching team with country code: {}", countryCode);
        Team team = teamRepository.findByCountryCode(countryCode)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with country code: " + countryCode));
        return teamMapper.toDTO(team);
    }

    public List<TeamDTO> getAllTeams() {
        log.info("Fetching all teams");
        return teamRepository.findAll().stream()
                .map(teamMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TeamDTO> searchTeams(String searchTerm) {
        log.info("Searching teams with term: {}", searchTerm);
        return teamRepository.searchTeams(searchTerm).stream()
                .map(teamMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<TeamDTO> getTeamsByConfederation(String confederation) {
        log.info("Fetching teams with confederation: {}", confederation);
        return teamRepository.findByConfederation(confederation).stream()
                .map(teamMapper::toDTO)
                .collect(Collectors.toList());
    }

    public TeamDTO updateTeam(Long id, TeamDTO teamDTO) {
        log.info("Updating team with ID: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));

        if (!team.getCountryCode().equals(teamDTO.getCountryCode()) &&
                teamRepository.existsByCountryCode(teamDTO.getCountryCode())) {
            throw new DuplicateResourceException("Team with country code '" + teamDTO.getCountryCode() + "' already exists");
        }

        team.setName(teamDTO.getName());
        team.setCountryCode(teamDTO.getCountryCode());
        team.setConfederation(teamDTO.getConfederation());

        Team updatedTeam = teamRepository.save(team);
        log.info("Team updated successfully with ID: {}", id);
        return teamMapper.toDTO(updatedTeam);
    }

    public void deleteTeam(Long id) {
        log.info("Deleting team with ID: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));
        teamRepository.delete(team);
        log.info("Team deleted successfully with ID: {}", id);
    }

    public boolean existsById(Long id) {
        return teamRepository.existsById(id);
    }

    public boolean existsByCountryCode(String countryCode) {
        return teamRepository.existsByCountryCode(countryCode);
    }
}

