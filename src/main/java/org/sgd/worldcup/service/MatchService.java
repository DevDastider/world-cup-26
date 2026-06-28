package org.sgd.worldcup.service;

import jakarta.validation.constraints.NotNull;
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
        match.setStatus(matchDTO.getStatus() != null ? matchDTO.getStatus() : MatchStatus.SCHEDULED);

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

        if (updatedMatch.getGroup() != null) {
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

    public MatchDTO updateMatchTeams(Long id, Long homeTeamId, Long awayTeamId) {
        log.info("Updating teams for match ID: {} (homeTeam= {}, awayTeam={})", id, homeTeamId, awayTeamId);

        Match match = matchRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + id));

        if (homeTeamId == null && awayTeamId == null) {
            throw new InvalidOperationException("Both homeTeam and awayTeam are null");
        }

        if (homeTeamId != null) {
            Team homeTeam = teamRepository.findById(homeTeamId)
                    .orElseThrow(() -> new ResourceNotFoundException("Home Team not found with ID: " + homeTeamId));
            match.setHomeTeam(homeTeam);
        }

        if (awayTeamId != null) {
            Team awayTeam = teamRepository.findById(awayTeamId)
                    .orElseThrow(() -> new ResourceNotFoundException("Away Team not found with ID: " + awayTeamId));
            match.setAwayTeam(awayTeam);
        }

        if (match.getHomeTeam() != null && match.getAwayTeam() != null && match.getHomeTeam().getId().equals(match.getAwayTeam().getId())) {
            throw new InvalidOperationException("Both homeTeam and awayTeam cannot be the same");
        }

        Match updated = matchRepository.save(match);
        log.info("Match updated successfully with ID: {}", id);
        return matchMapper.toDTO(updated);
    }

    public int resolveKnockoutSlot(Long placeholderTeamId, Long realTeamId, boolean deletePlaceholder) {
        log.info("Resolving slot for placeholder team: {}", placeholderTeamId);

        if (placeholderTeamId.equals(realTeamId)) {
            throw new InvalidOperationException("Placeholder team id cannot be the same with real team id");
        }

        Team placeholder = teamRepository.findById(placeholderTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("Placeholder team not found with ID: " + placeholderTeamId));
        Team real = teamRepository.findById(realTeamId)
                .orElseThrow(() -> new ResourceNotFoundException("Real team not found with ID: " + realTeamId));

        if (!placeholder.isPlaceholder()) {
            throw new InvalidOperationException("Team " + placeholderTeamId + " is not placeholder team");
        }
        if (real.isPlaceholder()) {
            throw new InvalidOperationException("Target Team " + realTeamId + " is a placeholder team");
        }

        List<Match> affected = matchRepository.findMatchesByTeamId(placeholderTeamId);
        int repointed = 0;

        for (Match match : affected) {
            boolean changed = false;
            if (match.getHomeTeam() != null && match.getHomeTeam().getId().equals(placeholderTeamId)) {
                match.setHomeTeam(real);
                changed = true;
            }
            if (match.getAwayTeam() != null && match.getAwayTeam().getId().equals(placeholderTeamId)) {
                match.setAwayTeam(real);
                changed = true;
            }
            if (changed) {
                matchRepository.save(match);
                repointed++;
            }
        }

        if (deletePlaceholder) {
            matchRepository.flush();
            teamRepository.delete(placeholder);
            log.info("Placeholder team deleted successfully with ID: {}", placeholderTeamId);
        }
        log.info("Resolved knockout slot for placeholder team: {}", placeholderTeamId);
        return repointed;
    }
}

