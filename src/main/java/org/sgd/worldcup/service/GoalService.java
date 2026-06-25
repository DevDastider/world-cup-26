package org.sgd.worldcup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.sgd.worldcup.dto.GoalDTO;
import org.sgd.worldcup.entity.Goal;
import org.sgd.worldcup.entity.Match;
import org.sgd.worldcup.entity.Player;
import org.sgd.worldcup.entity.Team;
import org.sgd.worldcup.exception.InvalidOperationException;
import org.sgd.worldcup.exception.ResourceNotFoundException;
import org.sgd.worldcup.mapper.GoalMapper;
import org.sgd.worldcup.repository.GoalRepository;
import org.sgd.worldcup.repository.MatchRepository;
import org.sgd.worldcup.repository.PlayerRepository;
import org.sgd.worldcup.repository.TeamRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class GoalService {
    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private GoalMapper goalMapper;

    public GoalDTO recordGoal(GoalDTO goalDTO) {
        log.info("Recording goal for match {} by player {}", goalDTO.getMatchId(), goalDTO.getPlayerId());

        Match match = matchRepository.findById(goalDTO.getMatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + goalDTO.getMatchId()));

        Player player = playerRepository.findById(goalDTO.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player not found with ID: " + goalDTO.getPlayerId()));

        Team scoringTeam = teamRepository.findById(goalDTO.getScoringTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + goalDTO.getScoringTeamId()));

        // Validate that player belongs to scoring team
        if (!player.getTeam().getId().equals(scoringTeam.getId())) {
            throw new InvalidOperationException("Player does not belong to the scoring team");
        }

        // Validate that scoring team is one of the match teams
        if (!match.getHomeTeam().getId().equals(scoringTeam.getId()) &&
            !match.getAwayTeam().getId().equals(scoringTeam.getId())) {
            throw new InvalidOperationException("Scoring team is not part of this match");
        }

        Goal goal = goalMapper.toEntity(goalDTO);
        goal.setMatch(match);
        goal.setPlayer(player);
        goal.setScoringTeam(scoringTeam);

        Goal savedGoal = goalRepository.save(goal);
        log.info("Goal recorded successfully with ID: {}", savedGoal.getId());
        return goalMapper.toDTO(savedGoal);
    }

    public GoalDTO getGoalById(Long id) {
        log.info("Fetching goal with ID: {}", id);
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + id));
        return goalMapper.toDTO(goal);
    }

    public List<GoalDTO> getAllGoals() {
        log.info("Fetching all goals");
        return goalRepository.findAll().stream()
                .map(goalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GoalDTO> getGoalsByMatch(Long matchId) {
        log.info("Fetching goals for match: {}", matchId);
        if (!matchRepository.existsById(matchId)) {
            throw new ResourceNotFoundException("Match not found with ID: " + matchId);
        }
        return goalRepository.findGoalsByMatchIdOrderByMinute(matchId).stream()
                .map(goalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GoalDTO> getGoalsByPlayer(Long playerId) {
        log.info("Fetching goals for player: {}", playerId);
        if (!playerRepository.existsById(playerId)) {
            throw new ResourceNotFoundException("Player not found with ID: " + playerId);
        }
        return goalRepository.findGoalsByPlayerIdOrderByDate(playerId).stream()
                .map(goalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GoalDTO> getGoalsByTeam(Long teamId) {
        log.info("Fetching goals for team: {}", teamId);
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with ID: " + teamId);
        }
        return goalRepository.findGoalsByTeamId(teamId).stream()
                .map(goalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public int getGoalCountByPlayer(Long playerId) {
        log.info("Getting goal count for player: {}", playerId);
        return goalRepository.countGoalsByPlayerId(playerId);
    }

    public int getGoalCountByTeamInMatch(Long teamId, Long matchId) {
        log.info("Getting goal count for team {} in match {}", teamId, matchId);
        return goalRepository.countGoalsByTeamInMatch(teamId, matchId);
    }

    public void deleteGoal(Long id) {
        log.info("Deleting goal with ID: {}", id);
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with ID: " + id));
        goalRepository.delete(goal);
        log.info("Goal deleted successfully with ID: {}", id);
    }

    public boolean existsById(Long id) {
        return goalRepository.existsById(id);
    }
}

