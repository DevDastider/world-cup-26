package org.sgd.worldcup.service;

import lombok.extern.slf4j.Slf4j;
import org.sgd.worldcup.dto.MatchDTO;
import org.sgd.worldcup.entity.Group;
import org.sgd.worldcup.entity.Match;
import org.sgd.worldcup.entity.Team;
import org.sgd.worldcup.enums.MatchStatus;
import org.sgd.worldcup.exception.InvalidOperationException;
import org.sgd.worldcup.exception.ResourceNotFoundException;
import org.sgd.worldcup.mapper.MatchMapper;
import org.sgd.worldcup.repository.GroupRepository;
import org.sgd.worldcup.repository.MatchRepository;
import org.sgd.worldcup.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class MatchService {
    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupTeamService groupTeamService;

    @Autowired
    private MatchMapper matchMapper;

    public MatchDTO createMatch(MatchDTO matchDTO) {
        log.info("Creating new match between teams {} and {}", matchDTO.getHomeTeamId(), matchDTO.getAwayTeamId());

        if (matchDTO.getHomeTeamId().equals(matchDTO.getAwayTeamId())) {
            throw new InvalidOperationException("Home team and away team cannot be the same");
        }

        Team homeTeam = teamRepository.findById(matchDTO.getHomeTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Home team not found with ID: " + matchDTO.getHomeTeamId()));

        Team awayTeam = teamRepository.findById(matchDTO.getAwayTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Away team not found with ID: " + matchDTO.getAwayTeamId()));

        Group group = null;
        if (matchDTO.getGroupId() != null) {
            group = groupRepository.findById(matchDTO.getGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + matchDTO.getGroupId()));
        }

        Match match = matchMapper.toEntity(matchDTO);
        match.setHomeTeam(homeTeam);
        match.setAwayTeam(awayTeam);
        match.setGroup(group);
        match.setStatus(matchDTO.getStatus()!=null ? matchDTO.getStatus() : MatchStatus.SCHEDULED);

        Match savedMatch = matchRepository.save(match);
        log.info("Match created successfully with ID: {}", savedMatch.getId());
        return matchMapper.toDTO(savedMatch);
    }

    public MatchDTO getMatchById(Long id) {
        log.info("Fetching match with ID: {}", id);
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));
        return matchMapper.toDTO(match);
    }

    public List<MatchDTO> getAllMatches() {
        log.info("Fetching all matches");
        return matchRepository.findAll().stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getUpcomingMatches() {
        log.info("Fetching upcoming matches");
        return matchRepository.findUpcomingMatches(MatchStatus.SCHEDULED).stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getCompletedMatches() {
        log.info("Fetching completed matches");
        return matchRepository.findCompletedMatches(MatchStatus.COMPLETED).stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getMatchesByTeam(Long teamId) {
        log.info("Fetching matches for team: {}", teamId);
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }
        return matchRepository.findMatchesByTeamId(teamId).stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getMatchesByGroup(Long groupId) {
        log.info("Fetching matches for group: {}", groupId);
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with ID: " + groupId);
        }
        return matchRepository.findByGroupId(groupId).stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<MatchDTO> getMatchesBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching matches between {} and {}", startDate, endDate);
        return matchRepository.findMatchesBetweenDates(startDate, endDate).stream()
                .map(matchMapper::toDTO)
                .collect(Collectors.toList());
    }

    public MatchDTO updateMatchResult(Long id, MatchDTO matchDTO) {
        log.info("Updating match result for match ID: {}", id);
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));

        match.setHomeTeamGoals(matchDTO.getHomeTeamGoals());
        match.setAwayTeamGoals(matchDTO.getAwayTeamGoals());
        match.setStatus(matchDTO.getStatus());

        Match updatedMatch = matchRepository.save(match);
        log.info("Match result updated successfully for match ID: {}", id);

        if (updatedMatch.getGroup()!=null) {
            groupTeamService.recalculateStandings(updatedMatch.getGroup().getId());
        }

        return matchMapper.toDTO(updatedMatch);
    }

    public void deleteMatch(Long id) {
        log.info("Deleting match with ID: {}", id);
        Match match = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));
        matchRepository.delete(match);
        log.info("Match deleted successfully with ID: {}", id);
    }

    public boolean existsById(Long id) {
        return matchRepository.existsById(id);
    }
}

